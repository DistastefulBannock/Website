package me.bannock.website.models.header;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@ControllerAdvice
public class HeaderAdvice {

    private final Logger logger = LogManager.getLogger();

    @Value("${bannock.header.randomHeaderLinksMaxCollisionRerolls}")
    private int maxCollisionRerolls = 10;

    /**
     * @return A map of header links to integer arrays with a size of two for
     * the respective x and y positions to put them in
     */
    @ModelAttribute("headerLinksToPositions")
    public Map<HeaderLink, Integer[]> getHeaderLinks(Principal principal){
        // TODO: Add some service so we can easily pull these values from a table
        List<HeaderLink> headerLinks = new ArrayList<>();

        // The widths and heights are from chrome dev tools. They are close enough
        if (principal == null)
            headerLinks.add(new HeaderLink("Login", "/core/login", 58, 40));
        else
            headerLinks.add(new HeaderLink("Logout", "/core/logout", 71, 41));
        headerLinks.add(new HeaderLink("The ramblings", "/blog/", 132, 40));
        headerLinks.add(new HeaderLink("Source code", "https://github.com/DistastefulBannock/Website",
                116, 41));
        headerLinks.add(new HeaderLink("About me", "/about/", 95, 41));

        Map<HeaderLink, Integer[]> headerLinksToPositions = new HashMap<>();
        int headerWidth = 800;
        int headerHeight = 90; // Slightly smaller than the header image because it's the area we want to draw in
        for (HeaderLink headerLink : headerLinks){
            int x, y;
            int rolls = 0;
            do{
                x = getPositionForBoundInRange(headerWidth, headerLink.getWidth());
                y = getPositionForBoundInRange(headerHeight, headerLink.getWidth());
            }while(rolls++ < maxCollisionRerolls && isBoundingWithOtherLink(headerLinksToPositions, headerLink, x, y));
            if (rolls >= maxCollisionRerolls)
                logger.warn("Hit the collision reroll limit for header links, rolls={}, maxCollisionRerolls={}, newLink={}",
                        rolls - 1, maxCollisionRerolls, headerLink);
            headerLinksToPositions.put(headerLink, new Integer[]{x, y});
        }
        return headerLinksToPositions;
    }

    private boolean isBoundingWithOtherLink(Map<HeaderLink, Integer[]> headerLinksToPositions, HeaderLink newLink,
                                            int newLinkX, int newLinkY){
        int newLinkWidth = newLink.getWidth();
        int newLinkHeight = newLink.getHeight();
        for (HeaderLink link : headerLinksToPositions.keySet()){
            int width = link.getWidth();
            int height = link.getHeight();
            int x = headerLinksToPositions.get(link)[0];
            int y = headerLinksToPositions.get(link)[1];
            if (newLinkX < x + width
                    && newLinkX + newLinkWidth > x
                    && newLinkY < y + height
                    && newLinkY + newLinkHeight > y)
                return true;
        }
        return false;
    }

    /**
     * Calculates a random point within an area that the provided bound will fit in.
     * If the bound is greater than the area, the bound is ignored and a random point is chosen
     * @param areaSize The total size of the area that the bound is going in
     * @param boundSize The bound's size
     * @return A random position that the bound can be put into
     */
    private int getPositionForBoundInRange(int areaSize, int boundSize){
        if (boundSize > areaSize){
            areaSize += boundSize;
        }
        areaSize -= boundSize;
        return ThreadLocalRandom.current().nextInt(areaSize + 1);
    }

}

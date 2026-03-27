package me.bannock.website.controllers.about;

import jakarta.servlet.http.HttpServletRequest;
import me.bannock.website.services.analytics.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/about")
public class AboutController {

    @Autowired
    public AboutController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    private final AnalyticsService analyticsService;

    @GetMapping("/")
    public String getAboutMePage(HttpServletRequest request){
//        analyticsService.createInstance(re)
        return "about/home";
    }

}

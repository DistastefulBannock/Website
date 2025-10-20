package me.bannock.website.config.security.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bannock.website.services.user.User;
import me.bannock.website.services.user.UserService;
import me.bannock.website.services.user.UserServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserAuthSuccessHandlerImpl extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    public UserAuthSuccessHandlerImpl(UserService userService){
        this.userService = userService;
    }

    private final UserService userService;
    private final Logger logger = LogManager.getLogger();

    @Value("${bannock.useCloudflareIpHeaderWhenAvailable}")
    private boolean useCloudflareIpHeaderWhenAvailable;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            User user = userService.getUserWithName(authentication.getName());

            String remoteIp = request.getRemoteAddr();
            if (useCloudflareIpHeaderWhenAvailable && request.getHeader("CF-Connecting-IP") != null){
                remoteIp = request.getHeader("CF-Connecting-IP");
            }
            userService.setUserLastIp(user.getId(), remoteIp);
            logger.info("User logged in, name={}, id={}, ip={}", user.getName(), user.getId(), remoteIp);
        } catch (UserServiceException e) {
            logger.warn("Something went wrong while logging user's last IP on login, userFriendlyMessage={}",
                    e.getUserFriendlyError(), e);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

}

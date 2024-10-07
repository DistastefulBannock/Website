package me.bannock.website.security.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;

public class AuthFailHandlerImpl implements AuthenticationFailureHandler {

    private final Logger logger = LogManager.getLogger();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        logger.warn("Something went wrong when user attempted to log in", exception);
        FlashMap flashMap = new FlashMap();
        // Don't send the AuthenticationException object itself because it has no default constructor and cannot be re-instantiated.
        flashMap.put("loginError", exception.getMessage());
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
        response.sendRedirect("/core/login");
    }

}

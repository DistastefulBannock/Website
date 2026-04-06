package me.bannock.website.security;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint {

    public static final String ORIGINAL_PATH_ATTRIBUTE = "bnok-request-original-path";

    private final Logger logger = LogManager.getLogger();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        logger.warn("User attempted to access resource without authentication, path={}", request.getRequestURI(), authException);
        request.setAttribute(ORIGINAL_PATH_ATTRIBUTE, request.getRequestURI());
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, authException);
        request.getRequestDispatcher("/error").forward(request, response);
    }

}

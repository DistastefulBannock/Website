package me.bannock.website.controllers;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ControllerUtils {

    /**
     * @return the logged in user's authentication, or null if not logged in or anonymous session
     */
    public static Authentication getAuthNoAnon(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken)
            return null;
        return auth;
    }

}

package me.bannock.website.controllers.core;

import brave.Tracer;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoreErrorController implements ErrorController {

    @Autowired
    private Tracer tracer;

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model){
        String userFriendlyMessage = "Something went wrong while processing your request";
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (exceptionObj instanceof Exception) {
            Exception exception = (Exception) exceptionObj;
            if (exception instanceof InsufficientAuthenticationException){
                userFriendlyMessage = exception.getMessage();
            }
        }

        model.addAttribute("tId", tracer.currentSpan().context().traceIdString());
        model.addAttribute("userFriendlyMessage", userFriendlyMessage);
        return "core/error";
    }

}

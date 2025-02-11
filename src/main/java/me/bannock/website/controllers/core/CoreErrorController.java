package me.bannock.website.controllers.core;

import brave.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoreErrorController implements ErrorController {

    @Autowired
    private Tracer tracer;

    @GetMapping("/error")
    public String handleError(Model model){
        model.addAttribute("tId", tracer.currentSpan().context().traceIdString());
        model.addAttribute("userFriendlyMessage",
                "Something went wrong while processing your request");
        return "core/error";
    }
}

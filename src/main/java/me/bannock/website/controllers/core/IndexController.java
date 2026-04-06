package me.bannock.website.controllers.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping("")
    public String getIndex(HttpServletRequest request) throws IOException {
        if (request.getQueryString() != null){
            return "forward:/core/?%s".formatted(request.getQueryString());
        }else{
            return "forward:/core/";
        }
    }

}

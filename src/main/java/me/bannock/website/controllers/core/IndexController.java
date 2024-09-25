package me.bannock.website.controllers.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping("")
    public void getIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("/core/?%s".formatted(request.getQueryString()));
    }

}

package me.bannock.website.controllers.core;

import me.bannock.website.services.user.User;
import me.bannock.website.services.user.UserService;
import me.bannock.website.services.user.UserServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@Controller
@RequestMapping("/core")
public class CoreFunctionalityController {

//    @Autowired
//    public CoreFunctionalityController(PasswordEncoder passwordEncoder, UserService userService){
//        this.passwordEncoder = passwordEncoder;
//        this.userService = userService;
//    }
//
//    private final PasswordEncoder passwordEncoder;
//    private final UserService userService;

    @GetMapping(path = "/")
    public String getIndex(Model model){
        return "core/home";
    }

    @GetMapping(path = "/login")
    public String getLogin(Model model){
        return "core/login";
    }

    @GetMapping(path = "/register")
    public String getRegister(Model model){
        return "core/register";
    }

    @PostMapping(path = "/register")
    @ResponseBody
    public String postRegister(Model model) throws UserServiceException {
//        User newUser = new User(-1, "admin", "admin@bannock.me", "127.1.0.0",
//                passwordEncoder.encode("password"), new ArrayList<>(),
//                true, false, false, false);
//        newUser = userService.registerUser(newUser);
//        return newUser.toString();
        return "Registrations are not being accepted at this time.";
    }

}

package com.inconcert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    @GetMapping("/home")
    public String home() {
        return "/home";
    }


    @GetMapping("/loginform")
    public String loginform() {
        return "/loginform";
    }

    @GetMapping("/registerform")
    public String registerform() {
        return "/registerform";
    }
}

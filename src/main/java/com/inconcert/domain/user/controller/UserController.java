package com.inconcert.domain.user.controller;

import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/loginform")
    public String loginform() {
        return "/loginform";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "/loginform";
    }
}

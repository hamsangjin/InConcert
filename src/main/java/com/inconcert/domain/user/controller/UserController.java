package com.inconcert.domain.user.controller;

import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/home")
    public String home() {
        return "/home";
    }

    @GetMapping("/loginform")
    public String loginform() {
        return "/loginform";
    }

    // 회원가입 폼
    @GetMapping("/registerform")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "/register";
    }

    // 회원가입
    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user) {
        userService.joinUser(user);
        return "redirect:/home";
    }
}

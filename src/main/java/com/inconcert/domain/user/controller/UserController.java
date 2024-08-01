package com.inconcert.domain.user.controller;

import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

    // Test
    @ResponseBody
    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("Access granted!");
    }
}

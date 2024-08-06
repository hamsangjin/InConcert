package com.inconcert.domain.user.controller;

import com.inconcert.domain.user.dto.request.FindIdReqDto;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/loginform")
    public String loginform() {
        return "/loginform";
    }

    @GetMapping("/registerform")
    public String registerform() {
        return "/registerform";
    }

    // 아이디 찾기
    @GetMapping("/idform")
    public String idform() {
        return "user/idform";
    }
}

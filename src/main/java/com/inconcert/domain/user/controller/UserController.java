package com.inconcert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    @GetMapping("/loginform")
    public String loginform() {
        return "loginform";
    }

    @GetMapping("/registerform")
    public String registerform() {
        return "registerform";
    }

    // 아이디 찾기
    @GetMapping("/idform")
    public String idform() {
        return "user/findidform";
    }

    // 비밀번호 찾기
    @GetMapping("/findpwform")
    public String findpw() {
        return "user/findpwform";
    }
}

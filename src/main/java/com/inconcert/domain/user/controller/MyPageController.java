package com.inconcert.domain.user.controller;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.domain.user.entity.Mbti;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.MyPageService;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final UserService userService;
    private final MyPageService myPageService;

    @GetMapping
    public String mypage(Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        model.addAttribute("user", user);
        return "/user/mypage";
    }

    @GetMapping("/editform")
    public String editMyPage(Model model) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        model.addAttribute("user", user);
        model.addAttribute("mbtiValues", Mbti.values());
        return "/user/mypageedit";
    }

    @PostMapping("/edit")
    public String editMyPage(@ModelAttribute("user") User user, MyPageEditReqDto reqDto) {
        myPageService.editUser(reqDto);
        return "redirect:/mypage";
    }

    @GetMapping("/board/{userId}")
    public String mypageBoard(Model model, @PathVariable("userId") Long userId) {
        List<PostDto> posts = myPageService.mypageBoard(userId);
        model.addAttribute("posts", posts);
        model.addAttribute("title", "board");
        return "/user/mypage-detail";
    }

    @GetMapping("/comment/{userId}")
    public String mypageComment(Model model, @PathVariable("userId") Long userId) {
        List<PostDto> posts = myPageService.mypageComment(userId);
        model.addAttribute("posts", posts);
        model.addAttribute("title", "comment");

        return "/user/mypage-detail";
    }

    @GetMapping("/like/{userId}")
    public String mypageLike(Model model, @PathVariable("userId") Long userId) {
        List<PostDto> posts = myPageService.mypageLike(userId);
        model.addAttribute("posts", posts);
        model.addAttribute("title", "like");
        return "/user/mypage-detail";
    }

    @GetMapping("/with/{userId}")
    public String mypageWith(Model model, @PathVariable("userId") Long userId) {
        List<PostDto> posts = myPageService.mypageBoard(userId);
        model.addAttribute("posts", posts);
        return "/user/mypage-detail";
    }

    @PostMapping("/bye")
    public String mypageBye(Model model){
        userService.deleteUser();
        return "redirect:/logout";
    }
}
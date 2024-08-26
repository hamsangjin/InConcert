package com.inconcert.domain.user.controller;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.feedback.service.FeedbackService;
import com.inconcert.domain.user.dto.request.MyPageEditReqDto;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import com.inconcert.domain.user.dto.response.FeedbackRspDTO;
import com.inconcert.domain.user.entity.Mbti;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.MyPageService;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final UserService userService;
    private final MyPageService myPageService;
    private final FeedbackService feedbackService;

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
    public String editMyPage(@ModelAttribute MyPageEditReqDto reqDto, RedirectAttributes redirectAttributes) {
        try {
            myPageService.editUser(reqDto);
        }
        catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 존재하는 닉네임이나 이메일을 입력하였습니다.");
            return "redirect:/mypage/editform";
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "입력한 내용을 다시 확인해주세요.");
            return "redirect:/mypage/editform";
        }
        return "redirect:/mypage";
    }

    // 기본 프로필로 변경
    @PostMapping("/reset-profile-image")
    @ResponseBody
    public ResponseEntity<Void> resetProfileImage() {
        myPageService.resetToDefaultProfileImage();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/board/{userId}")
    public String showMyPosts(Model model,
                              @PathVariable("userId") Long userId,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<PostDTO> postPage = myPageService.getMyPosts(userId, page, size);

        model.addAttribute("postsPage", postPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("title", "board");

        return "/user/mypage-detail";
    }

    @GetMapping("/comment/{userId}")
    public String showMyCommentPosts(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<PostDTO> postPage = myPageService.getMyCommentPosts(userId, page, size);

        model.addAttribute("postsPage", postPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("title", "comment");

        return "/user/mypage-detail";
    }

    @GetMapping("/like/{userId}")
    public String showMyLikePosts(Model model,
                                  @PathVariable("userId") Long userId,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<PostDTO> postPage = myPageService.getMyLikePosts(userId, page, size);
        model.addAttribute("postsPage", postPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("title", "like");

        return "/user/mypage-detail";
    }

    @GetMapping("/match/{userId}/present")
    public String showMyPresentMatch(Model model,
                                     @PathVariable(name = "userId") Long userId,
                                     @RequestParam(name = "page", defaultValue = "0") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<MatchRspDTO> matchRspDTOS = myPageService.presentMatch(userId, page, size);
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        model.addAttribute("matchRspDTOs", matchRspDTOS);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "present");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", matchRspDTOS.getTotalPages());

        return "/user/match-list";
    }

    @GetMapping("/match/{userId}/complete")
    public String showMyCompleteMatch(Model model,
                                      @PathVariable(name = "userId") Long userId,
                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size) {

        Page<MatchRspDTO> matchRspDTOS = myPageService.completeMatch(userId, page, size);
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        List<Boolean> isEndFeedback = myPageService.isEndFeedback(userId, matchRspDTOS
                .stream()
                .map(MatchRspDTO::getPostId)
                .collect(Collectors.toList()));

        model.addAttribute("matchRspDTOs", matchRspDTOS);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "complete");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", matchRspDTOS.getTotalPages());
        model.addAttribute("isEndFeedback", isEndFeedback);

        return "/user/match-list";
    }

    @GetMapping("/match/{userId}/complete/{postId}")
    public String showMyReviewee(Model model,
                                 @PathVariable(name = "userId") Long userId,
                                 @PathVariable(name = "postId") Long postId) {
        List<FeedbackRspDTO> myReviewee = myPageService.getMyReviewee(userId, postId);
        List<Boolean> myReviewStatuses = myPageService.getUsersReviewStatuses(userId, postId);
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        model.addAttribute("user", user);
        model.addAttribute("myReviewee", myReviewee);
        model.addAttribute("myReviewStatuses", myReviewStatuses);
        return "/user/review-list";
    }

    @PostMapping("/match/{reviewerId}/complete/{postId}/{revieweeId}")
    public ResponseEntity<?> addMyfeedback(@PathVariable(name = "reviewerId") Long reviewerId,
                                @PathVariable(name = "revieweeId") Long revieweeId,
                                @PathVariable(name = "postId") Long postId,
                                @RequestParam(name = "rating") int rating) {

        feedbackService.feedback(postId, reviewerId, revieweeId, rating);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/bye")
    public String deleteUser(){
        userService.deleteUser();
        return "redirect:/logout";
    }
}
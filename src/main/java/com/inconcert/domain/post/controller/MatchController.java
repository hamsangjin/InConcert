package com.inconcert.domain.post.controller;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.MatchService;
import com.inconcert.domain.post.service.WriteService;
import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.service.ReportService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final UserService userService;
    private final EditService editService;
    private final WriteService writeService;
    private final ReportService reportService;

    @GetMapping
    public String match(Model model) {
        model.addAttribute("Musicalposts", matchService.getAllMatchPostsByPostCategory("musical"));
        model.addAttribute("Concertposts", matchService.getAllMatchPostsByPostCategory("concert"));
        model.addAttribute("Theaterposts", matchService.getAllMatchPostsByPostCategory("theater"));
        model.addAttribute("Etcposts", matchService.getAllMatchPostsByPostCategory("etc"));
        model.addAttribute("categoryTitle", "match");
        return "board/board";
    }

    @GetMapping("/{postCategoryTitle}")
    public String matchDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size,
                              Model model) {

        Page<PostDTO> postsPage = matchService.getAllInfoPostsByPostCategory(postCategoryTitle, page, size);

        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);

        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/search")
    public String search(@PathVariable("postCategoryTitle") String postCategoryTitle,
                         @RequestParam(name = "keyword") String keyword,
                         @RequestParam(name = "period", required = false, defaultValue = "all") String period,
                         @RequestParam(name = "type", required = false, defaultValue = "title+content") String type,
                         @RequestParam(name = "gender", required = false, defaultValue = "all") String gender,
                         @RequestParam(name = "mbti", required = false, defaultValue = "all") String mbti,
                         @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                         @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                         Model model) {

        Page<PostDTO> postsPage = matchService.findByKeywordAndFilters(postCategoryTitle, keyword, period, type, gender, mbti, page, size);

        Map<String, String> searchInfo = new HashMap<>();
        searchInfo.put("period", period);
        searchInfo.put("type", type);
        searchInfo.put("keyword", keyword);
        searchInfo.put("gender", gender);
        searchInfo.put("mbti", mbti);
        model.addAttribute("searchInfo", searchInfo);

        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());

        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        model.addAttribute("post", matchService.getPostDtoByPostId(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        model.addAttribute("createForm", new CommentCreateForm());

        // 연결된 채팅방 확인
        boolean hasChatRoom = matchService.checkPostHasChatRoom(postId);
        model.addAttribute("hasChatRoom", String.valueOf(hasChatRoom));
        return "board/post-detail";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId,
                             RedirectAttributes redirectAttributes) {
        try {
            matchService.deletePost(postId);
        }
        // 연결된 채팅방이 있으면 삭제 불가
        catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/match/" + postCategoryTitle + "/" + postId;
        }

        return "redirect:/match/" + postCategoryTitle;
    }

    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPostForm(@PathVariable("postCategoryTitle") String postCategoryTitle,
                               @PathVariable("postId") Long postId, Model model) {
        PostDTO postDto = matchService.getPostDtoByPostId(postId);

        // 채팅방과 연결된 포스트가 있으면 수정 불가
        boolean hasChatRoom = matchService.checkPostHasChatRoom(postId);
        model.addAttribute("hasChatRoom", String.valueOf(hasChatRoom));

        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "match");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute PostDTO postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "match", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDTO postDto) {
        Post post = writeService.save(postDto);
        return "redirect:/match/" + post.getPostCategory().getTitle() + '/' + post.getId();
    }

    @GetMapping("/{postCategoryTitle}/{postId}/report")
    public String reportForm(@PathVariable("postId") Long postId,
                             Model model) {

        model.addAttribute("reportDTO", new ReportDTO());
        model.addAttribute("post", matchService.getPostDtoByPostId(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "match");
        return "report/reportform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/report")
    public String report(@PathVariable("postId") Long postId,
                         @PathVariable("postCategoryTitle") String postCategoryTitle,
                         @ModelAttribute ReportDTO reportDTO){

        reportService.report(postId, "match", reportDTO.getType());
        return "redirect:/match" + '/' + postCategoryTitle + '/' + postId;
    }
}
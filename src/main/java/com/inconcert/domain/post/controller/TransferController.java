package com.inconcert.domain.post.controller;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.service.EditService;
import com.inconcert.domain.post.service.TransferService;
import com.inconcert.domain.post.service.WriteService;
import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.service.ReportService;
import com.inconcert.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final UserService userService;
    private final EditService editService;
    private final WriteService writeService;
    private final ReportService reportService;

    @Value("${kakao.javascript-key}")
    private String kakaoKey;

    @GetMapping
    public String transfer(Model model) {
        model.addAttribute("Musicalposts", transferService.getAllTransferPostsByPostCategory("musical"));
        model.addAttribute("Concertposts", transferService.getAllTransferPostsByPostCategory("concert"));
        model.addAttribute("Theaterposts", transferService.getAllTransferPostsByPostCategory("theater"));
        model.addAttribute("Etcposts", transferService.getAllTransferPostsByPostCategory("etc"));
        model.addAttribute("categoryTitle", "transfer");
        return "board/board";
    }

    @GetMapping("/{postCategoryTitle}")
    public String detailTransfer(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                 Model model) {

        Page<PostDTO> postsPage = transferService.getAllInfoPostsByPostCategory(postCategoryTitle, page, size);

        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);

        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/search")
    public String search(@PathVariable("postCategoryTitle") String postCategoryTitle,
                         @RequestParam(name = "keyword") String keyword,
                         @RequestParam(name = "period", required = false, defaultValue = "all") String period,
                         @RequestParam(name = "type", required = false, defaultValue = "title+content") String type,
                         @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                         @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                         Model model) {

        Page<PostDTO> postsPage = transferService.getByKeywordAndFilters(postCategoryTitle, keyword, period, type, page, size);

        Map<String, String> searchInfo = new HashMap<>();
        searchInfo.put("period", period);
        searchInfo.put("type", type);
        searchInfo.put("keyword", keyword);
        model.addAttribute("searchInfo", searchInfo);

        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postsPage", postsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());

        return "board/board-detail";
    }

    @GetMapping("/{postCategoryTitle}/{postId}")
    public String getPostDetail(@PathVariable("postCategoryTitle") String postCategoryTitle,
                                @PathVariable("postId") Long postId, Model model) {
        PostDTO postDTO = transferService.getPostDtoByPostId(postId);
        if(postDTO == null) {
            return "redirect:/home";
        } else{
            model.addAttribute("post", postDTO);
        }
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        model.addAttribute("createForm", new CommentCreationDTO());
        model.addAttribute("kakaoKey", kakaoKey);

        return "board/post-detail";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/delete")
    public String deletePost(@PathVariable("postCategoryTitle") String postCategoryTitle,
                             @PathVariable("postId") Long postId) {
        transferService.deletePost(postId);
        return "redirect:/transfer/" + postCategoryTitle;
    }

    @GetMapping("/{postCategoryTitle}/{postId}/edit")
    public String editPostForm(@PathVariable("postCategoryTitle") String postCategoryTitle,
                               @PathVariable("postId") Long postId, Model model) {
        PostDTO postDto = transferService.getPostDtoByPostId(postId);


        model.addAttribute("post", postDto);
        model.addAttribute("categoryTitle", "transfer");
        model.addAttribute("postCategoryTitle", postCategoryTitle);
        return "board/editform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/edit")
    public String updatePost(@PathVariable("postId") Long postId,
                             @ModelAttribute PostDTO postDto,
                             @RequestParam("newCategoryTitle") String newCategoryTitle,
                             @RequestParam("newPostCategoryTitle") String newPostCategoryTitle) {
        Long updatedPostId = editService.updatePost(postId, postDto, "transfer", newCategoryTitle, newPostCategoryTitle);
        return "redirect:/" + newCategoryTitle + '/' + newPostCategoryTitle + '/' + updatedPostId;
    }

    @PostMapping("/write")
    public String write(@ModelAttribute PostDTO postDto) {
        Post post = writeService.save(postDto);
        return "redirect:/transfer/" + post.getPostCategory().getTitle() + '/' + post.getId();
    }

    @GetMapping("/{postCategoryTitle}/{postId}/report")
    public String reportForm(@PathVariable("postId") Long postId,
                             Model model) {

        model.addAttribute("reportDTO", new ReportDTO());
        model.addAttribute("post", transferService.getPostDtoByPostId(postId));
        model.addAttribute("user", userService.getAuthenticatedUser());
        model.addAttribute("categoryTitle", "transfer");
        return "report/reportform";
    }

    @PostMapping("/{postCategoryTitle}/{postId}/report")
    public String report(@PathVariable("postId") Long postId,
                         @PathVariable("postCategoryTitle") String postCategoryTitle,
                         @ModelAttribute ReportDTO reportDTO){

        reportService.report(postId, "transfer", reportDTO.getType());
        return "redirect:/transfer" + '/' + postCategoryTitle + '/' + postId;
    }
}
package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import com.inconcert.global.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EditService {
    private final InfoRepository infoRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;
    private final TransferRepository transferRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long updatePost(Long postId, PostDto postDto, String currentCategoryTitle, String newCategoryTitle, String newPostCategoryTitle) {
        // 현재 카테고리에서 게시글 찾기
        Post currentPost = findPostByIdAndCategory(postId, currentCategoryTitle);

        Category category = categoryRepository.findByTitle(newCategoryTitle)
                .orElseThrow(() -> new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));

        List<PostCategory> postCategories = postCategoryRepository.findByTitle(newPostCategoryTitle);

        // 적절한 PostCategory 찾기
        PostCategory postCategory = postCategories.stream()
                .filter(pc -> pc.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_COMBINATION_NOT_FOUND.getMessage()));

        postDto.setThumbnailUrl(extractURL(postDto.getContent()));

        // 새로운 레포지토리에 저장
        Post updatedPost = Post.builder()
                .id(currentPost.getId())
                .title(postDto.getTitle())
                .thumbnailUrl(postDto.getThumbnailUrl())
                .content(postDto.getContent())
                .endDate(postDto.getEndDate())
                .matchCount(postDto.getMatchCount())
                .user(currentPost.getUser())
                .comments(new ArrayList<>(currentPost.getComments()))
                .likes(new HashSet<>(currentPost.getLikes()))
                .viewCount(currentPost.getViewCount())
                .postCategory(postCategory)
                .build();

        savePostToRepository(updatedPost, newCategoryTitle);
        return updatedPost.getId();
    }

    private Post findPostByIdAndCategory(Long postId, String categoryTitle) {
        return getRepositoryByCategoryTitle(categoryTitle).findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    private void savePostToRepository(Post post, String categoryTitle) {
        getRepositoryByCategoryTitle(categoryTitle).save(post);
    }

    private JpaRepository<Post, Long> getRepositoryByCategoryTitle(String categoryTitle) {
        return switch (categoryTitle.toLowerCase()) {
            case "info" -> infoRepository;
            case "match" -> matchRepository;
            case "review" -> reviewRepository;
            case "transfer" -> transferRepository;
            default -> throw new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage());
        };
    }

    private static String extractURL(String input) {
        // img의 src속성만 추출하는 패턴 생성
        String regex = "http[s]?://[^\\s\"']+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        String url = null;
        // 패턴에 맞는거 하나라도 찾을 경우 url에 저장 후 리턴
        while (matcher.find()) {
            url = matcher.group();
            break;
        }

        return url;
    }
}
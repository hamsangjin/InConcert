package com.inconcert.global.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.repository.HomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final InfoRepository infoRepository;
    private final ReviewRepository reviewRepository;
    private final MatchRepository matchRepository;
    private final TransferRepository transferRepository;
    private final HomeRepository homeRepository;

    @Transactional(readOnly = true)
    public List<PostDto> getAllCategoryPosts(String categoryTitle) {
        List<Post> posts = switch (categoryTitle) {
            case "info" -> infoRepository.findPostsByCategoryTitleInfo();
            case "review" -> reviewRepository.findPostsByCategoryTitleReview();
            case "match" -> matchRepository.findPostsByCategoryTitleMatch();
            case "transfer" -> transferRepository.findPostsByCategoryTitleTransfer();
            default -> throw new CategoryNotFoundException(categoryTitle + "라는 카테고리를 찾을 수 없습니다.");
        };

        List<PostDto> postDtos = getPostDtos(posts);
        return postDtos;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findByKeyword(String keyword) {
        List<Post> posts = homeRepository.findByKeyword(keyword);

        List<PostDto> postDtos = getPostDtos(posts);

        return postDtos;
    }

    @Transactional(readOnly = true)
    public List<PostDto> findLatestPostsByPostCategory(){
        List<Post> posts = infoRepository.findLatestPostsByPostCategory();
        List<PostDto> postDtos = getPostDtos(posts);
        return postDtos;
    }

    private static List<PostDto> getPostDtos(List<Post> posts) {
        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {

            PostCategory postCategory = post.getPostCategory();
            Category category = postCategory.getCategory();

            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .category(category)
                    .postCategory(postCategory)
                    .thumbnailUrl(post.getThumbnailUrl())
                    .nickname(post.getUser().getNickname())
                    .viewCount(post.getViewCount())
                    .commentCount(post.getComments().size())
                    .likeCount(post.getLikes().size())
                    .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                    .createdAt(post.getCreatedAt())
                    .build();

            postDtos.add(postDto);
        }
        return postDtos;
    }
}
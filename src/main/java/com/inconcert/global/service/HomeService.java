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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {
    private final InfoRepository infoRepository;
    private final ReviewRepository reviewRepository;
    private final MatchRepository matchRepository;
    private final TransferRepository transferRepository;
    private final HomeRepository homeRepository;

    public List<PostDto> getAllCategoryPosts(String categoryTitle) {
        PageRequest pageable = PageRequest.of(0, 8);
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<PostDto> posts = switch (categoryTitle) {
            case "info" -> infoRepository.findPostsByCategoryTitle(pageable, yesterday);
            case "review" -> reviewRepository.findPostsByCategoryTitle(pageable, yesterday);
            case "match" -> matchRepository.findPostsByCategoryTitle(pageable, yesterday);
            case "transfer" -> transferRepository.findPostsByCategoryTitle(pageable, yesterday);
            default -> throw new CategoryNotFoundException(categoryTitle + "라는 카테고리를 찾을 수 없습니다.");
        };

        return posts;
    }

    public List<PostDto> findByKeyword(String keyword) {
        List<Post> posts = homeRepository.findByKeyword(keyword);

        List<PostDto> postDtos = getPostDtos(posts);

        return postDtos;
    }

    public List<PostDto> findLatestPostsByPostCategory(){
        return infoRepository.findLatestPostsByPostCategory();
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
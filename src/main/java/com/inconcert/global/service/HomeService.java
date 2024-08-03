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

    @Transactional(readOnly = true)
    public List<PostDto> getAllCategoryPosts(String categoryTitle) {
        List<Post> posts;
        if(categoryTitle.equals("Info"))            posts = infoRepository.findPostsByCategoryTitleInfo();
        else if(categoryTitle.equals("Review"))     posts = reviewRepository.findPostsByCategoryTitleReview();
        else if(categoryTitle.equals("Match"))      posts = matchRepository.findPostsByCategoryTitleMatch();
        else if(categoryTitle.equals("Transfer"))   posts = transferRepository.findPostsByCategoryTitleTransfer();
        else                                        throw new CategoryNotFoundException(categoryTitle + "라는 카테고리를 찾을 수 없습니다.");

        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {

            PostCategory postCategory = post.getPostCategory();
            Category category = postCategory.getCategory();

            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .category(category)
                    .postCategory(postCategory)
                    .username(post.getUser().getUsername())
                    .viewSize(post.getViewCount())
                    .commentSize(post.getComments().size())
                    .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                    .createdAt(post.getCreatedAt())
                    .build();

            postDtos.add(postDto);
        }
        return postDtos;
    }
}

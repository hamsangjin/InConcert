package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import com.inconcert.global.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final TransferRepository transferRepository;
    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<PostDto> getAllTransferPostsByPostCategory(String postCategoryTitle) {
        List<Post> posts;
        if(postCategoryTitle.equals("musical"))         posts = transferRepository.findPostsByPostCategoryTitle("musical");
        else if(postCategoryTitle.equals("concert"))    posts = transferRepository.findPostsByPostCategoryTitle("concert");
        else if(postCategoryTitle.equals("theater"))    posts = transferRepository.findPostsByPostCategoryTitle("theater");
        else if(postCategoryTitle.equals("etc"))        posts = transferRepository.findPostsByPostCategoryTitle("etc");
        else                                            throw new PostCategoryNotFoundException(postCategoryTitle);

        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .postCategory(post.getPostCategory())
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

    @Transactional(readOnly = true)
    public PostDto getPostById(Long postId) {
        Post post = transferRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("ID가 " + postId + "인 게시물을 찾을 수 없습니다."));

        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .postCategory(post.getPostCategory())
                .nickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .commentCount(post.getComments().size())
                .comments(post.getComments())
                .likeCount(post.getLikes().size())
                .isNew(Duration.between(post.getCreatedAt(), LocalDateTime.now()).toDays() < 1)
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Transactional
    public void save(PostDto postDto){

        // 게시물 작성 폼에서 가져온 postCategory 제목으로 조회해서 PostCategory 리스트 생성
        List<PostCategory> postCategories = postCategoryRepository.findByTitle(postDto.getPostCategoryTitle());

        // 게시물 작성 폼에서 가져온 Category 제목으로 조회해서 Category 객체 생성
        Category category = categoryRepository.findByTitle(postDto.getCategoryTitle())
                .orElseThrow(() -> new CategoryNotFoundException(postDto.getCategoryTitle() + "라는 Category를 찾지 못했습니다."));

        // 적절한 PostCategory 찾기
        PostCategory postCategory = postCategories.stream()
                .filter(pc -> pc.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new PostCategoryNotFoundException("해당 제목과 카테고리 조합의 PostCategory를 찾지 못했습니다."));

        // 생성한 Category를 builder를 통해 연관관계 주입
        PostCategory updatedPostCategory = postCategory.builder()
                .id(postCategory.getId())
                .title(postCategory.getTitle())
                .category(category)
                .build();

        postDto.setUser(userService.getAuthenticatedUser());

        // 주입된 PostCategory를 Post에 저장
        Post post = PostDto.toEntity(postDto, updatedPostCategory);

        transferRepository.save(post);
    }
}
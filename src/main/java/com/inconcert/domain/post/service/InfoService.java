package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.user.repository.UserRepository;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.PostCategoryNotFoundException;
import com.inconcert.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InfoService {
    private final InfoRepository infoRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PostDto> getAllInfoPostsByPostCategory(String postCategoryTitle) {
        List<Post> posts;
        if(postCategoryTitle.equals("Musical"))         posts = infoRepository.findPostsByPostCategoryTitle("Musical");
        else if(postCategoryTitle.equals("Concert"))    posts = infoRepository.findPostsByPostCategoryTitle("Concert");
        else if(postCategoryTitle.equals("Theater"))    posts = infoRepository.findPostsByPostCategoryTitle("Theater");
        else if(postCategoryTitle.equals("Etc"))        posts = infoRepository.findPostsByPostCategoryTitle("Etc");
        else                                            throw new PostCategoryNotFoundException(postCategoryTitle);

        List<PostDto> postDtos = new ArrayList<>();
        for (Post post : posts) {
            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .postCategory(post.getPostCategory())
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

    @Transactional
    public void save(PostDto postDto){
        // 게시물 작성 폼에서 가져온 postCategory 제목으로 조회해서 PostCategory 객체 생성
        PostCategory postCategory = postCategoryRepository.findByTitle(postDto.getPostCategoryTitle())
                .orElseThrow(() -> new PostCategoryNotFoundException(postDto.getPostCategoryTitle() + "라는 Post Category를 찾지 못했습니다."));

        // 게시물 작성 폼에서 가져온 Category 제목으로 조회해서 Category 객체 생성
        Category category = categoryRepository.findByTitle(postDto.getCategoryTitle())
                .orElseThrow(() -> new CategoryNotFoundException(postDto.getCategoryTitle() + "라는 Category를 찾지 못했습니다."));

        // 생성한 Category를 builder를 통해 연관관계 주입
        PostCategory updatedPostCategory = postCategory.builder()
                .id(postCategory.getId())
                .title(postCategory.getTitle())
                .category(category)
                .build();

        postDto.setUser(userRepository.findByUsername("admin").orElseThrow(()->new UserNotFoundException("user를 찾을수 없습니다.")));

        // 주입된 PostCategory를 Post에 저장
        Post post = PostDto.toEntity(postDto, updatedPostCategory);

        infoRepository.save(post);
    }
}

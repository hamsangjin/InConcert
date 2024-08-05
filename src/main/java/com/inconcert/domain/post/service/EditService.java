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
import com.inconcert.global.exception.PostCategoryNotFoundException;
import com.inconcert.global.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        if (!currentCategoryTitle.equals(newCategoryTitle)) {
            // 카테고리가 변경된 경우
            // 기존 레포지토리에서 삭제
            deletePostByIdAndCategory(postId, currentCategoryTitle);
            Category category = categoryRepository.findByTitle(newCategoryTitle)
                    .orElseThrow(() -> new CategoryNotFoundException("찾으려는 Category가 존재하지 않습니다."));

            List<PostCategory> postCategories = postCategoryRepository.findByTitle(newPostCategoryTitle);

            // 적절한 PostCategory 찾기
            PostCategory postCategory = postCategories.stream()
                    .filter(pc -> pc.getCategory().equals(category))
                    .findFirst()
                    .orElseThrow(() -> new PostCategoryNotFoundException("해당 제목과 카테고리 조합의 PostCategory를 찾지 못했습니다."));

            // 새로운 레포지토리에 저장
            Post updatedPost = Post.builder()
                    .title(postDto.getTitle())
                    .content(postDto.getContent())
                    .postCategory(postCategory)
                    .endDate(postDto.getEndDate())
                    .matchCount(postDto.getMatchCount())
                    .user(currentPost.getUser())
                    .build();
            savePostToRepository(updatedPost, newCategoryTitle);
            return updatedPost.getId();
        } else {
            // 카테고리가 변경되지 않은 경우에도 postCategory를 업데이트
            Category category = categoryRepository.findByTitle(currentCategoryTitle)
                    .orElseThrow(() -> new CategoryNotFoundException("찾으려는 Category가 존재하지 않습니다."));

            List<PostCategory> postCategories = postCategoryRepository.findByTitle(newPostCategoryTitle);

            // 적절한 PostCategory 찾기
            PostCategory postCategory = postCategories.stream()
                    .filter(pc -> pc.getCategory().equals(category))
                    .findFirst()
                    .orElseThrow(() -> new PostCategoryNotFoundException("해당 제목과 카테고리 조합의 PostCategory를 찾지 못했습니다."));

            currentPost.update(postDto.getTitle(), postDto.getContent(), postCategory, postDto.getEndDate(), postDto.getMatchCount());
            savePostToRepository(currentPost, currentCategoryTitle);
            return currentPost.getId();
        }


    }

    private Post findPostByIdAndCategory(Long postId, String categoryTitle) {
        switch (categoryTitle.toLowerCase()) { // 카테고리 이름을 소문자로 변환하여 비교
            case "info":
                return infoRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("찾으려는 Post가 존재하지 않습니다."));
            case "match":
                return matchRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("찾으려는 Post가 존재하지 않습니다."));
            case "review":
                return reviewRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("찾으려는 Post가 존재하지 않습니다."));
            case "transfer":
                return transferRepository.findById(postId)
                        .orElseThrow(() -> new PostNotFoundException("찾으려는 Post가 존재하지 않습니다."));
            default:
                throw new PostNotFoundException("카테고리가 잘못되었습니다.");
        }
    }


    private void deletePostByIdAndCategory(Long postId, String categoryTitle) {
        switch (categoryTitle.toLowerCase()) {
            case "info":
                infoRepository.deleteById(postId);
                break;
            case "match":
                matchRepository.deleteById(postId);
                break;
            case "review":
                reviewRepository.deleteById(postId);
                break;
            case "transfer":
                transferRepository.deleteById(postId);
                break;
            default:
                throw new PostNotFoundException("카테고리가 잘못되었습니다.");
        }
    }

    private void savePostToRepository(Post post, String categoryTitle) {
        switch (categoryTitle.toLowerCase()) {
            case "info":
                infoRepository.save(post);
                break;
            case "match":
                matchRepository.save(post);
                break;
            case "review":
                reviewRepository.save(post);
                break;
            case "transfer":
                transferRepository.save(post);
                break;
            default:
                throw new PostNotFoundException("카테고리가 잘못되었습니다.");
        }
    }
}

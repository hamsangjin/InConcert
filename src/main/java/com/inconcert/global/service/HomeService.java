package com.inconcert.global.service;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.repository.HomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<PostDto> findLatestPostsByPostCategory(){
        return infoRepository.findLatestPostsByPostCategory();
    }

    public List<PostDto> getAllCategoryPosts(String categoryTitle) {
        PageRequest pageable = PageRequest.of(0, 8);

        return switch (categoryTitle) {
            case "info" -> infoRepository.findPostsByCategoryTitle(pageable);
            case "review" -> reviewRepository.findPostsByCategoryTitle(pageable);
            case "match" -> matchRepository.findPostsByCategoryTitle(pageable);
            case "transfer" -> transferRepository.findPostsByCategoryTitle(pageable);
            default -> throw new CategoryNotFoundException(categoryTitle + "라는 카테고리를 찾을 수 없습니다.");
        };
    }

    public Page<PostDto> findByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return homeRepository.findByKeyword(keyword, pageable);
    }
}
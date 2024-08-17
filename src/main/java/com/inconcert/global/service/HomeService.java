package com.inconcert.global.service;

import com.inconcert.domain.post.dto.PostDto;
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

import java.time.LocalDateTime;
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

        return switch (categoryTitle) {
            case "info" -> infoRepository.findPostsByCategoryTitle(pageable, yesterday);
            case "review" -> reviewRepository.findPostsByCategoryTitle(pageable, yesterday);
            case "match" -> matchRepository.findPostsByCategoryTitle(pageable, yesterday);
            case "transfer" -> transferRepository.findPostsByCategoryTitle(pageable, yesterday);
            default -> throw new CategoryNotFoundException(categoryTitle + "라는 카테고리를 찾을 수 없습니다.");
        };
    }

    public List<PostDto> findByKeyword(String keyword) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return homeRepository.findByKeyword(keyword, yesterday);
    }

    public List<PostDto> findLatestPostsByPostCategory(){
        return infoRepository.findLatestPostsByPostCategory();
    }
}
package com.inconcert.common.service;

import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.common.exception.CategoryNotFoundException;
import com.inconcert.common.exception.ExceptionMessage;
import com.inconcert.common.repository.HomeRepository;
import com.inconcert.domain.post.dto.PostDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
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

    public List<PostDTO> getAllPostDTOsByCategoryTitle(String categoryTitle) {
        PageRequest pageable = PageRequest.of(0, 8);

        return switch (categoryTitle) {
            case "info" -> infoRepository.findPostsByCategoryTitle(pageable);
            case "review" -> reviewRepository.findPostsByCategoryTitle(pageable);
            case "match" -> matchRepository.findPostsByCategoryTitle(pageable);
            case "transfer" -> transferRepository.findPostsByCategoryTitle(pageable);
            default -> throw new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage());
        };
    }

    public Page<PostDTO> getPostDTOsByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return homeRepository.findByKeyword(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getPopularPosts() {
        List<String> categories = Arrays.asList("musical", "concert", "theater", "etc");
        List<PostDTO> popularPosts = new ArrayList<>();

        for (String category : categories) {
            popularPosts.add(infoRepository.findPopularPostByPostCategoryTitle(category).orElse(null));
        }
        return popularPosts;
    }
}
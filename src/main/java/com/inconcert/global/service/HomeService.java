package com.inconcert.global.service;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.global.exception.CategoryNotFoundException;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.repository.HomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {
    private final InfoRepository infoRepository;
    private final ReviewRepository reviewRepository;
    private final MatchRepository matchRepository;
    private final TransferRepository transferRepository;
    private final HomeRepository homeRepository;

    public List<PostDTO> getAllCategoryPosts(String categoryTitle) {
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
    public List<PostDTO> getLatestPosts() {
        PageRequest pageRequest = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt"));
        return infoRepository.findTop8LatestInfoPosts(pageRequest);
    }

    @Transactional(readOnly = true)
    public List<PostDTO> getPopularPosts() {
        List<String> categories = Arrays.asList("musical", "concert", "theater", "etc");
        List<PostDTO> popularPosts = new ArrayList<>();

        for (String category : categories) {
            PostDTO popularPost = infoRepository.findFirstPostByPostCategoryTitle(category)
                    .orElse(null);
            if (popularPost != null) {
                popularPosts.add(popularPost);
            }
        }

        return popularPosts;
    }
}
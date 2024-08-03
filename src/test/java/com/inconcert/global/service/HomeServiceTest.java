package com.inconcert.global.service;

import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HomeServiceTest {
    @Autowired
    private InfoRepository infoRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private PostCategoryRepository postCategoryRepository;

    @Test
    void 홈_게시물_불러오기() {
        // given

        // when

        // then
    }
}
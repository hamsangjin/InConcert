package com.inconcert.domain.category.entity;

import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class InitCategory implements ApplicationRunner {
    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 중복 확인
        if (categoryRepository.count() > 0) {
            return;
        }

        // Categories 삽입
        Category info = new Category("info");
        Category review = new Category("review");
        Category match = new Category("match");
        Category transfer = new Category("transfer");

        categoryRepository.save(info);
        categoryRepository.save(review);
        categoryRepository.save(match);
        categoryRepository.save(transfer);

        // PostCategories 삽입
        postCategoryRepository.save(new PostCategory("musical", info));
        postCategoryRepository.save(new PostCategory("concert", info));
        postCategoryRepository.save(new PostCategory("theater", info));
        postCategoryRepository.save(new PostCategory("etc", info));

        postCategoryRepository.save(new PostCategory("musical", review));
        postCategoryRepository.save(new PostCategory("concert", review));
        postCategoryRepository.save(new PostCategory("theater", review));
        postCategoryRepository.save(new PostCategory("etc", review));

        postCategoryRepository.save(new PostCategory("musical", match));
        postCategoryRepository.save(new PostCategory("concert", match));
        postCategoryRepository.save(new PostCategory("theater", match));
        postCategoryRepository.save(new PostCategory("etc", match));

        postCategoryRepository.save(new PostCategory("musical", transfer));
        postCategoryRepository.save(new PostCategory("concert", transfer));
        postCategoryRepository.save(new PostCategory("theater", transfer));
        postCategoryRepository.save(new PostCategory("etc", transfer));
    }
}
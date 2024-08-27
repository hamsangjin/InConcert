package com.inconcert.domain.category.repository;

import com.inconcert.domain.category.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    List<PostCategory> findByTitle(String title);
    @Query("SELECT pc FROM PostCategory pc JOIN FETCH pc.category WHERE pc.title = :title AND pc.category.title = :categoryTitle")
    Optional<PostCategory> findByTitleAndCategoryTitleWithCategory(@Param("title") String title, @Param("categoryTitle") String categoryTitle);
}
package com.inconcert.domain.post.repository;

import com.inconcert.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InfoRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle")
    List<Post> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "WHERE c.title = 'info'")
    List<Post> findPostsByCategoryTitleInfo();

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "JOIN FETCH p.user u " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle " +
            "AND ((:type = 'title+content' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)) " +
            "OR   (:type = 'title' AND p.title LIKE %:keyword%) " +
            "OR   (:type = 'content' AND p.content LIKE %:keyword%) " +
            "OR   (:type = 'author' AND u.nickname LIKE %:keyword%)) " +
            "AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Post> findByKeywordAndFilters(@Param("postCategoryTitle") String postCategoryTitle,
                                       @Param("keyword") String keyword,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("type") String type);

    // 크롤링 후 post category의 1~4번까지 지우기
    @Transactional
    @Query("DELETE Post p WHERE p.postCategory.id BETWEEN 1 AND 4")
    void afterCrawling();
}
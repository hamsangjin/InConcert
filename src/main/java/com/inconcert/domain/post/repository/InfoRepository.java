package com.inconcert.domain.post.repository;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InfoRepository extends JpaRepository<Post, Long> {
    // /home에서 인기 공연 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDto(p.id, pc.title, p.thumbnailUrl) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "WHERE c.title = 'info' AND p.createdAt IN (" +
            "SELECT MIN(p2.createdAt) FROM Post p2 " +
            "JOIN p2.postCategory pc2 " +
            "JOIN pc2.category c2 " +
            "WHERE c2.title = 'info' " +
            "GROUP BY pc2.id) ")
    List<PostDto> findLatestPostsByPostCategory();

    // /home에서 공연 소식 게시물 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDto(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' ")
    List<PostDto> findPostsByCategoryTitle(Pageable pageable);

    // /info에서 게시물들 카테고리에 맞게 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDto(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle")
    List<PostDto> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    // /info/categoryTitle에서 게시물들 알맞게 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDto(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle")
    Page<PostDto> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle,
                                               Pageable pageable);

    // /info/categoryTitle에서 검색한 경우 검색 결과 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDto(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle " +
            "AND ((:type = 'titleContent' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)) " +
            "OR   (:type = 'title' AND p.title LIKE %:keyword%) " +
            "OR   (:type = 'content' AND p.content LIKE %:keyword%) " +
            "OR   (:type = 'author' AND u.nickname LIKE %:keyword%)) " +
            "AND p.createdAt BETWEEN :startDate AND :endDate")
    Page<PostDto> findByKeywordAndFilters(@Param("postCategoryTitle") String postCategoryTitle,
                                          @Param("keyword") String keyword,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("type") String type,
                                          Pageable pageable);

    // 크롤링 후 post category의 1~4번까지 지우기
    @Modifying
    @Query("DELETE Post p WHERE p.postCategory.id BETWEEN 1 AND 4")
    void afterCrawling();
}
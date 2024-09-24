package com.inconcert.domain.post.repository;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InfoRepository extends JpaRepository<Post, Long> {
    // /home에서 공연 소식 게시물 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' " +
            "ORDER BY p.createdAt DESC")
    List<PostDTO> findPostsByCategoryTitle(Pageable pageable);

    // info 포스트 상위 8개 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' " +
            "ORDER BY p.createdAt DESC")
    List<PostDTO> getTop8LatestInfoPosts(Pageable pageable);

    // 실시간 인기 공연 게시글 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' " +
            "AND p.createdAt = (SELECT p2.createdAt " +
                                "FROM Post p2 " +
                                "WHERE p2.postCategory = p.postCategory " +
                                "AND p2.postCategory = pc " +
                                "AND p2.postCategory.category = c " +
                                "ORDER BY p2.popularity DESC, p2.createdAt ASC " +
                                "LIMIT 1) " +
            "AND pc.title = :postCategoryTitle")
    Optional<PostDTO> findPopularPostByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    // /info에서 게시물들 카테고리에 맞게 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle " +
            "ORDER BY p.createdAt DESC")
    List<PostDTO> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    // /info/categoryTitle에서 게시물들 알맞게 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'info' AND pc.title = :postCategoryTitle " +
            "ORDER BY p.createdAt DESC")
    Page<PostDTO> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle,
                                               Pageable pageable);

    // /info/categoryTitle에서 검색한 경우 검색 결과 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
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
            "AND p.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY p.createdAt DESC")
    Page<PostDTO> findByKeywordAndFilters(@Param("postCategoryTitle") String postCategoryTitle,
                                          @Param("keyword") String keyword,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("type") String type,
                                          Pageable pageable);

    Boolean existsByTitle(String title);

    Optional<Post> findByTitle(String title);
}
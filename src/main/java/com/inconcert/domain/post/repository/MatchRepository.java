package com.inconcert.domain.post.repository;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Post, Long> {
    // /home에서 동행 정보 게시물 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'match' AND p.isEnd = false " +
            "ORDER BY p.createdAt DESC")
    List<PostDTO> findPostsByCategoryTitle(Pageable pageable);

    // /match 게시물들 카테고리에 맞게 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'match' AND pc.title = :postCategoryTitle AND p.isEnd = false " +
            "ORDER BY p.createdAt DESC")
    List<PostDTO> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    // /match/categoryTitle에서 게시물들 알맞게 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'match' AND pc.title = :postCategoryTitle AND p.isEnd = false " +
            "ORDER BY p.createdAt DESC")
    Page<PostDTO> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle,
                                               Pageable pageable);

    // /match/categoryTitle에서 검색한 경우 검색 결과 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE c.title = 'match' AND pc.title = :postCategoryTitle " +
            "AND ((:type = 'titleContent' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)) " +
            "OR   (:type = 'title' AND p.title LIKE %:keyword%) " +
            "OR   (:type = 'content' AND p.content LIKE %:keyword%) " +
            "OR   (:type = 'author' AND u.nickname LIKE %:keyword%)) " +
            "AND p.createdAt BETWEEN :startDate AND :endDate " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (:mbti IS NULL OR u.mbti = :mbti)" +
            "AND p.isEnd = false " +
            "ORDER BY p.createdAt DESC")
    Page<PostDTO> findByKeywordAndFilters(@Param("postCategoryTitle") String postCategoryTitle,
                                          @Param("keyword") String keyword,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("type") String type,
                                          @Param("gender") Gender gender,
                                          @Param("mbti") Mbti mbti,
                                          Pageable pageable);

    @Query("SELECT p.id " +
            "FROM Post p " +
            "WHERE p.endDate < :currentDate AND p.isEnd = false AND p.postCategory.category.title = 'match'")
    List<Long> findAllByEndDateBeforeAndIsEndFalse(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT new com.inconcert.domain.user.dto.response.MatchRspDTO" +
            "(p.id, p.chatRoom.id, p.title, p.endDate, size(p.matchUserIds), p.matchCount, p.isEnd, p.thumbnailUrl, p.postCategory.category.title, p.postCategory.title, p.user.nickname) " +
            "FROM Post p " +
            "WHERE :userId member of p.matchUserIds AND p.isEnd = true")
    Page<MatchRspDTO> findAllByUserIdInMatchUserIdsAndEndMatch(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT id " +
            "FROM User " +
            "WHERE id IN (" +
            "    SELECT p.matchUserIds " +
            "    FROM Post p" +
            "    WHERE p.id = :postId) " +
            "AND id <> :userId")
    List<Long> findMatchUsersByPostId(@Param("postId") Long postId, @Param("userId") Long userId);
}
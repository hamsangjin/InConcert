package com.inconcert.domain.post.repository;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.Gender;
import com.inconcert.domain.user.entity.Mbti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "WHERE c.title = 'match' AND pc.title = :postCategoryTitle")
    List<Post> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "WHERE c.title = 'match'")
    List<Post> findPostsByCategoryTitleMatch();

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "JOIN FETCH p.user u " +
            "WHERE c.title = 'match' AND pc.title = :postCategoryTitle " +
            "AND ((:type = 'title+content' AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)) " +
            "OR   (:type = 'title' AND p.title LIKE %:keyword%) " +
            "OR   (:type = 'content' AND p.content LIKE %:keyword%) " +
            "OR   (:type = 'author' AND u.nickname LIKE %:keyword%)) " +
            "AND p.createdAt BETWEEN :startDate AND :endDate " +
            "AND (:gender IS NULL OR u.gender = :gender) " +
            "AND (:mbti IS NULL OR u.mbti = :mbti)")
    List<Post> findByKeywordAndFilters(@Param("postCategoryTitle") String postCategoryTitle,
                                       @Param("keyword") String keyword,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("type") String type,
                                       @Param("gender") Gender gender,
                                       @Param("mbti") Mbti mbti);
}

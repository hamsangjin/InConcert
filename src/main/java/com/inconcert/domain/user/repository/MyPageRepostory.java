package com.inconcert.domain.user.repository;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MyPageRepostory extends JpaRepository<Post, Long> {

    // 내가 작성한 게시물들
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE u.id = :userId")
    Page<PostDTO> findByUserId(@Param("userId") Long userId,
                               Pageable pageable);

    // 내가 작성한 댓글이 있는 게시물들
    @Query("SELECT DISTINCT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.comments cm " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE cm.user.id = :userId")
    Page<PostDTO> findPostsWithMyComments(@Param("userId") Long userId,
                                          Pageable pageable);

    // 내가 좋아요를 누른 게시물들
    @Query("SELECT DISTINCT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.likes l " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE l.user.id = :userId")
    Page<PostDTO> findPostsILiked(@Param("userId") Long userId,
                                  Pageable pageable);
}
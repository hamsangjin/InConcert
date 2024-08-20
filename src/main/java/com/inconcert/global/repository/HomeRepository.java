package com.inconcert.global.repository;

import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HomeRepository extends JpaRepository<Post, Long> {
    // 전체 검색할 때 게시물 불러오기
    @Query("SELECT new com.inconcert.domain.post.dto.PostDTO(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN TIMESTAMPDIFF(HOUR, CURRENT_TIMESTAMP, p.createdAt) < 24 THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<PostDTO> findByKeyword(@Param("keyword") String keyword,
                                Pageable pageable);
}
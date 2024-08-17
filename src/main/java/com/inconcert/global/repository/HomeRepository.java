package com.inconcert.global.repository;

import com.inconcert.domain.post.dto.PostDto;
import com.inconcert.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HomeRepository extends JpaRepository<Post, Long> {
    @Query("SELECT new com.inconcert.domain.post.dto.PostDto(p.id, p.title, c.title, pc.title, p.thumbnailUrl, u.nickname, " +
            "p.viewCount, SIZE(p.likes), SIZE(p.comments), " +
            "CASE WHEN p.createdAt > :yesterday THEN true ELSE false END, p.createdAt) " +
            "FROM Post p " +
            "JOIN p.postCategory pc " +
            "JOIN pc.category c " +
            "JOIN p.user u " +
            "WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<PostDto> findByKeyword(@Param("keyword") String keyword,
                                       @Param("yesterday") LocalDateTime yesterday);
}

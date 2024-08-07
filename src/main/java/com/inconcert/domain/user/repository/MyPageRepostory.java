package com.inconcert.domain.user.repository;

import com.inconcert.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyPageRepostory extends JpaRepository<Post, Long> {
    // 내가 작성한 게시물들
    List<Post> findByUserId(Long userId);

    // 내가 작성한 댓글이 있는 게시물들
    @Query("SELECT DISTINCT p FROM Post p JOIN p.comments c WHERE c.user.id = :userId")
    List<Post> findPostsWithMyComments(@Param("userId") Long userId);

    // 내가 좋아요를 누른 게시물들
    @Query("SELECT DISTINCT p FROM Post p JOIN p.likes l WHERE l.user.id = :userId")
    List<Post> findPostsILiked(@Param("userId") Long userId);
}

package com.inconcert.domain.like.repository;

import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like,Long> {

    Optional<Like> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
}

package com.inconcert.domain.comment.repository;

import com.inconcert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Modifying
    @Query("UPDATE Comment c " +
            "SET c.content = :content, c.isSecret = :isSecret " +
            "WHERE c.id = :id")
    void updateComment(@Param("id") Long id, @Param("content") String content, @Param("isSecret") Boolean isSecret);
}

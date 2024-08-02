package com.inconcert.domain.post.repository;

import com.inconcert.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "WHERE c.title = 'Transfer' AND pc.title = :postCategoryTitle")
    List<Post> findPostsByPostCategoryTitle(@Param("postCategoryTitle") String postCategoryTitle);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.postCategory pc " +
            "JOIN FETCH pc.category c " +
            "WHERE c.title = 'Transfer'")
    List<Post> findPostsByCategoryTitleTransfer();
}

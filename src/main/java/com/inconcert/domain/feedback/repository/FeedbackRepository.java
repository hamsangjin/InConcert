package com.inconcert.domain.feedback.repository;

import com.inconcert.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT f.reviewee.id " +
            "FROM Feedback f " +
            "WHERE f.reviewer.id = :reviewerId AND f.reviewee.id IN :revieweeIds AND f.post.id = :postId")
    List<Long> findExistingFeedbacks(@Param("reviewerId") Long reviewerId,
                                     @Param("revieweeIds") List<Long> revieweeIds,
                                     @Param("postId") Long postId);

    @Query("SELECT f.reviewee.id FROM Feedback f WHERE f.reviewer.id = :reviewerId")
    List<Long> findRevieweeIdsByReviewerId(@Param("reviewerId") Long reviewerId);
}

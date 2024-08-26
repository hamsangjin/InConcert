package com.inconcert.domain.user.repository;

import com.inconcert.domain.user.dto.response.FeedbackRspDTO;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByUsernameAndEmail(String username, String email);

    @Query("SELECT new com.inconcert.domain.user.dto.response.FeedbackRspDTO" +
            "(u.id, u.profileImage, u.nickname, u.birth, u.mbti, u.gender, :userId, :postId) " +
            "FROM User u " +
            "WHERE u.id IN :matchUserIds")
    List<FeedbackRspDTO> getFeedbackRspDTOByMatchUserIds(@Param("userId") Long userId,
                                                         @Param("postId") Long postId,
                                                         @Param("matchUserIds") List<Long> matchUserIds);

    @Modifying
    @Query(value = "UPDATE users u " +
            "SET u.manner_point = (SELECT ROUND(AVG(f.point), 2) FROM feedbacks f WHERE f.reviewee_id = :revieweeId) " +
            "WHERE u.id = :revieweeId", nativeQuery = true)
    void updateMannerPointByRevieweeId(@Param("revieweeId") Long revieweeId);
}

package com.inconcert.domain.user.repository;

import com.inconcert.domain.user.dto.response.FeedbackRspDTO;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
            "(u.id, u.profileImage, u.nickname, u.birth, u.mbti, u.gender) " +
            "FROM User u " +
            "WHERE u.id IN :matchUserIds AND u.id != :userId")
    List<FeedbackRspDTO> getFeedbackRspDTOByMatchUserIds(@Param("userId") Long userId, @Param("matchUserIds") List<Long> matchUserIds);


}

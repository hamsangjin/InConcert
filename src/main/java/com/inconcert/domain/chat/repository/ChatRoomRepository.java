package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT DISTINCT c FROM ChatRoom c " +
            "LEFT JOIN c.hostUser h " +
            "LEFT JOIN c.users u " +
            "WHERE h.id = :userId OR u.id = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

    // 두 유저가 포함된 1:1 채팅방이 존재하는지 확인
    List<ChatRoom> findByUsersContainsAndUsersContains(User user1, User user2);

    // 채팅방에 속한 유저 목록
    @Query("select c.users from ChatRoom c where c.id = :chatRoomId")
    List<User> findAllById(@Param("chatRoomId") Long chatRoomId);
}

package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.entity.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    @Modifying
    @Query("DELETE FROM ChatRoomUser cru " +
            "WHERE cru.user.id = :userId AND cru.chatRoom.id = :chatRoomId")
    void deleteByUserAndChatRoom(@Param("userId") Long userId,
                                 @Param("chatRoomId") Long chatRoomId);

    @Query("SELECT u.id " +
            "FROM ChatRoomUser cru " +
            "JOIN cru.user u " +  // cru.user와 JOIN
            "WHERE cru.chatRoom.id = :chatRoomId")  // chatRoom.id로 필터링
    List<Long> getChatRoomUserIdsByChatRoomId(@Param("chatRoomId") Long chatRoomId);


}
package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.entity.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    @Modifying
    @Query("DELETE FROM ChatRoomUser cru " +
            "WHERE cru.user.id = :userId AND cru.chatRoom.id = :chatRoomId")
    void deleteByUserAndChatRoom(@Param("userId") Long userId,
                                 @Param("chatRoomId") Long chatRoomId);

}
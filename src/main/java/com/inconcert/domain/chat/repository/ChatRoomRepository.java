package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {

    Optional<ChatRoom> findById(Long id);

    @Query("SELECT DISTINCT c FROM ChatRoom c " +
            "LEFT JOIN c.hostUser h " +
            "LEFT JOIN c.users u " +
            "WHERE h.id = :userId OR u.id = :userId")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);

}

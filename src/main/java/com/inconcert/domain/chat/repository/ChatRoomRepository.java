package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<ChatRoom> findByUsersContainsAndUsersContainsAndPostIsNull(User user1, User user2);

    // 채팅방에 속한 유저 목록
    @Query("select c.users from ChatRoom c where c.id = :chatRoomId")
    List<User> findAllById(@Param("chatRoomId") Long chatRoomId);

    ChatRoom findByPostId(Long postId);

    @Query("SELECT new com.inconcert.domain.user.dto.response.MatchRspDTO" +
            "(c.post.id, c.id, c.post.title, c.post.endDate, size(c.users), c.post.matchCount, c.post.isEnd, c.post.thumbnailUrl, c.post.postCategory.category.title, c.post.postCategory.title, c.hostUser.nickname) " +
            "FROM ChatRoom c JOIN c.users u " +
            "WHERE u.id = :userId AND c.post.isEnd = false")
    Page<MatchRspDTO> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}

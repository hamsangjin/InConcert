package com.inconcert.domain.chat.repository;

import com.inconcert.domain.chat.dto.ChatRoomDTO;
import com.inconcert.domain.chat.dto.UserDTO;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT new com.inconcert.domain.chat.dto.ChatRoomDTO(c.id, c.roomName, c.hostUser.id, SIZE(c.users), c.post.id)" +
            "FROM ChatRoom c " +
            "WHERE c.id = :chatRoomId")
    ChatRoomDTO getChatRoomDTOById(@Param("chatRoomId") Long chatRoomId);

    @Query(value = "SELECT c.id AS chatRoomId, c.room_name AS roomName, c.host_user_id AS hostUserId, COUNT(u.id) AS userCount, " +
            "(CASE " +
            "WHEN m.created_at IS NULL THEN NULL " +
            "WHEN TIMESTAMPDIFF(MINUTE, m.created_at, CURRENT_TIMESTAMP) < 1 THEN 'now' " +
            "WHEN TIMESTAMPDIFF(DAY, m.created_at, CURRENT_TIMESTAMP) >= 1 THEN 'day' " +
            "WHEN TIMESTAMPDIFF(HOUR, m.created_at, CURRENT_TIMESTAMP) >= 1 THEN 'hour' " +
            "ELSE 'minute' " +
            "END) AS timeSince, " +
            "(CASE " +
            "WHEN m.created_at IS NULL THEN NULL " +
            "WHEN TIMESTAMPDIFF(MINUTE, m.created_at, CURRENT_TIMESTAMP) < 1 THEN 0 " +
            "WHEN TIMESTAMPDIFF(DAY, m.created_at, CURRENT_TIMESTAMP) >= 1 THEN TIMESTAMPDIFF(DAY, m.created_at, CURRENT_TIMESTAMP) " +
            "WHEN TIMESTAMPDIFF(HOUR, m.created_at, CURRENT_TIMESTAMP) >= 1 THEN TIMESTAMPDIFF(HOUR, m.created_at, CURRENT_TIMESTAMP) " +
            "ELSE TIMESTAMPDIFF(MINUTE, m.created_at, CURRENT_TIMESTAMP) " +
            "END) AS diffTime " +
            "FROM chat_rooms c " +
            "LEFT JOIN chat_messages m ON m.created_at = (SELECT MAX(m2.created_at) FROM chat_messages m2 WHERE m2.chat_room_id = c.id) " +
            "LEFT JOIN chat_room_users cru ON c.id = cru.chat_room_id " +
            "LEFT JOIN users u ON cru.user_id = u.id " +
            "WHERE c.host_user_id = :userId OR :userId IN (SELECT cru2.user_id FROM chat_room_users cru2 WHERE cru2.chat_room_id = c.id) " +
            "GROUP BY c.id, c.room_name, c.host_user_id, m.created_at",
            nativeQuery = true)
    List<Map<String, Object>> getChatRoomDTOsByUserId(@Param("userId") Long userId);

    // 채팅방에 속한 유저 목록
    @Query("SELECT new com.inconcert.domain.chat.dto.UserDTO(u.id, u.username, u.nickname, u.profileImage)" +
            "FROM ChatRoom c " +
            "JOIN ChatRoomUser cru ON :chatRoomId = cru.chatRoom.id " +
            "JOIN User u ON u.id = cru.user.id " +
            "WHERE c.id = :chatRoomId")
    List<UserDTO> findAllById(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT count(c) " +
            "FROM ChatRoom c " +
            "JOIN ChatRoomUser cru ON c.id = cru.chatRoom.id " +
            "WHERE c.post IS NULL " +
            "AND :userId1 IN (SELECT u.id FROM cru.user u) " +
            "AND :userId2 IN (SELECT u.id FROM cru.user u) ")
    int findChatRoomsWithNoPostAndBothUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 동행 완료 버튼 클릭 시, 그때의 채팅방 유저들 불러오기
    @Query("SELECT u.user.id " +
            "FROM ChatRoom c JOIN c.users u " +
            "WHERE c.post.id = :postId")
    List<Long> findUserIdsByPostId(@Param("postId") Long postId);

    // 내 동행목록, 동행중인 정보 불러오기
    @Query("SELECT new com.inconcert.domain.user.dto.response.MatchRspDTO" +
            "(c.post.id, c.id, c.post.title, c.post.endDate, size(c.users), c.post.matchCount, c.post.isEnd, c.post.thumbnailUrl, c.post.postCategory.category.title, c.post.postCategory.title, c.hostUser.nickname) " +
            "FROM ChatRoom c JOIN c.users u " +
            "WHERE u.user.id = :userId AND c.post.isEnd = false")
    Page<MatchRspDTO> getChatRoomDTOsByUserId(@Param("userId") Long userId, Pageable pageable);
}
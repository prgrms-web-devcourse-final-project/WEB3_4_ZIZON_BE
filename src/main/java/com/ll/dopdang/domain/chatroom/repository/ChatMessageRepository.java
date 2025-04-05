package com.ll.dopdang.domain.chatroom.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.chatroom.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	// 채팅 온 시간에 대해 asc 처리
	List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

	// 최신 메시지 타임스탬프 조회 - JPQL로 수정
	@Query("SELECT MAX(cm.timestamp) FROM ChatMessage cm WHERE cm.roomId = :roomId")
	LocalDateTime findLatestMessageTimeByRoomId(@Param("roomId") String roomId);

	// 최근 메시지 조회 - Pageable 추가
	List<ChatMessage> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

	// 위의 메소드를 ChatService에서 사용하는 메소드명으로 별칭 추가
	default List<ChatMessage> findTopMessagesByRoomIdOrderByTimestampDesc(String roomId, int limit) {
		return findByRoomIdOrderByTimestampDesc(roomId, org.springframework.data.domain.PageRequest.of(0, limit));
	}
}
package com.ll.dopdang.domain.chatroom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.chatroom.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	//채팅 온 시간에 대해 asc 처리
	List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

}
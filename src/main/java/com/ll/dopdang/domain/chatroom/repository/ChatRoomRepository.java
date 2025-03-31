package com.ll.dopdang.domain.chatroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.chatroom.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
}

package com.ll.dopdang.domain.chatroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.chatroom.entity.ChatRoom;

@Repository
public interface ChatRepository extends JpaRepository<ChatRoom, Long> {
}

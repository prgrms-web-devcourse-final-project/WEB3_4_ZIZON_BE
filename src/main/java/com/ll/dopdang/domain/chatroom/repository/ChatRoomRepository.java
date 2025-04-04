package com.ll.dopdang.domain.chatroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.chatroom.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	//채팅방 생성 시 room이 존재하는 지 확인을 위한 메소드
	Optional<ChatRoom> findByRoomId(String roomId);

	//채팅방 이름 (사용자1:사용자2)에 로그인 한 사용자의 아이디가 존재하는지 채팅방 목록 확인
	List<ChatRoom> findByRoomIdContaining(String member);

}

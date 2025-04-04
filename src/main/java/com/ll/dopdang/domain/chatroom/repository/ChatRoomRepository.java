package com.ll.dopdang.domain.chatroom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ll.dopdang.domain.chatroom.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	//채팅방 생성 시 room이 존재하는 지 확인을 위한 메소드
	Optional<ChatRoom> findByRoomId(String roomId);

	@Query("SELECT c FROM ChatRoom c WHERE c.member1 = :member OR c.member2 = :member")
	List<ChatRoom> findByMember(@Param("member") String member);
}

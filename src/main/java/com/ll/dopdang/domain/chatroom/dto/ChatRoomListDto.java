package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomListDto {
	private String roomId;               // 채팅방 식별자
	private String otherUserName;        // 상대방 이름
	private String otherUserProfile;     // 상대방 프로필 이미지
	private String lastMessage;          // 마지막 메시지 내용
	private LocalDateTime lastMessageTime;  // 마지막 메시지 시간
	private long unreadCount;            // 안 읽은 메시지 개수
}

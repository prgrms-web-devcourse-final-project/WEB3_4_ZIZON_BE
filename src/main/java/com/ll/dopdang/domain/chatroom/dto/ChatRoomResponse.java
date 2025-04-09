package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
	private String roomId;
	private String sender;
	private String receiver;
	private String lastMessage;
	private LocalDateTime lastMessageTime;
	private int unreadCount;
	private Long projectId;
	private String otherUserName;
	private String otherUserProfile;
	private Long otherUserId;
}

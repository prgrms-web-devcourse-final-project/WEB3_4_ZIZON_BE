package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;

public record ChatRoomResponseDto(
	String roomId,
	String displayName,
	long unreadCount,
	String lastMessage,
	LocalDateTime lastMessageTime
) {
	public ChatRoomResponseDto(ChatRoom room, String currentUsername, long unreadCount, String lastMessage, LocalDateTime lastMessageTime) {
		this(room.getRoomId(), computeDisplayName(room, currentUsername), unreadCount, lastMessage, lastMessageTime);
	}

	private static String computeDisplayName(ChatRoom room, String currentUsername) {
		if (room.getMember1().equals(room.getMember2())) {
			return "나와의 채팅방";
		} else {
			if (currentUsername.equals(room.getMember1())) {
				return room.getMember2() + "님과의 채팅방";
			} else if (currentUsername.equals(room.getMember2())) {
				return room.getMember1() + "님과의 채팅방";
			} else {
				return room.getRoomId();
			}
		}
	}
}
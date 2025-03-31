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
		if (room.getClient().equals(room.getExpert())) {
			return "나와의 채팅방";
		} else {
			if (currentUsername.equals(room.getClient())) {
				return room.getExpert() + "님과의 채팅방";
			} else if (currentUsername.equals(room.getExpert())) {
				return room.getClient() + "님과의 채팅방";
			} else {
				return room.getRoomId();
			}
		}
	}
}
package com.ll.dopdang.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPayload {
	private String roomId;
	private String otherUserName;
	private String message;
	private int unreadCount;
}

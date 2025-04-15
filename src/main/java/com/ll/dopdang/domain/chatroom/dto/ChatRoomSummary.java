package com.ll.dopdang.domain.chatroom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomSummary {
	private String roomId;
	private String otherEmail;
	private String lastMessage;
	private String timestamp;  // ISO 문자열 형식
	private Long projectId;
}

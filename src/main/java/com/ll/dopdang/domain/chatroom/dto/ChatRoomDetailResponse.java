package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomDetailResponse {
	private String roomId;
	private String sender;
	private String receiver;
	private String content;
	private LocalDateTime timestamp;
	private String expert_name;
	private String expert_profile_image;
}

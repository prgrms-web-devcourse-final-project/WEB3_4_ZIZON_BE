package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;

import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.member.entity.Member;

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
	private String user_name;
	private String user_profile_image;
	private boolean read;
	private String fileUrl;
	public static ChatRoomDetailResponse from(ChatMessage message, Member senderMember, boolean read) {
		String senderName = senderMember != null ? senderMember.getName() : "";
		String profileImage = senderMember != null ? senderMember.getProfileImage() : "";
		return new ChatRoomDetailResponse(
			message.getRoomId(),
			message.getSender(),
			message.getReceiver(),
			message.getContent(),
			message.getTimestamp(),
			senderName,
			profileImage,
			read,
			message.getFileUrl()
		);
	}
}

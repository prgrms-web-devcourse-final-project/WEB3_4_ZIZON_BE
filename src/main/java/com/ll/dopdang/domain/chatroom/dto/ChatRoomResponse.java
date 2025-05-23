package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;

import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.member.entity.Member;

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
	private Long expertId;
	private boolean memberActive1;
	private boolean memberActive2;

	public static ChatRoomResponse from(ChatRoom chatRoom, Member currentMember,
		Member otherUser, ChatMessage lastMessage, int unreadCount) {
		ChatRoomResponse dto = new ChatRoomResponse();
		dto.setRoomId(chatRoom.getRoomId());
		if (chatRoom.getMember1().equalsIgnoreCase(currentMember.getEmail())) {
			dto.setSender(chatRoom.getMember1());
			dto.setReceiver(chatRoom.getMember2());
		} else {
			dto.setSender(chatRoom.getMember2());
			dto.setReceiver(chatRoom.getMember1());
		}

		if (lastMessage != null) {
			dto.setLastMessage(lastMessage.getContent());
			dto.setLastMessageTime(lastMessage.getTimestamp());
		}
		dto.setProjectId(chatRoom.getProjectId());
		if (otherUser != null) {
			dto.setOtherUserId(otherUser.getId());
			dto.setOtherUserName(otherUser.getName());
			dto.setOtherUserProfile(otherUser.getProfileImage());
			if (otherUser.getExpert() != null && otherUser.getExpert().getId() != null) {
				dto.setExpertId(otherUser.getExpert().getId());
			}else{
				dto.setExpertId(currentMember.getId());
			}
		}
		dto.setUnreadCount(unreadCount);
		dto.setMemberActive1(chatRoom.isMemberActive1());
		dto.setMemberActive2(chatRoom.isMemberActive2());
		return dto;
	}
}
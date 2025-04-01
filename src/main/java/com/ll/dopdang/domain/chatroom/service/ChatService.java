package com.ll.dopdang.domain.chatroom.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.repository.ChatMessageRepository;
import com.ll.dopdang.domain.chatroom.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;

	// 채팅방 생성 로직
	@Transactional
	public ChatRoom createChatRoom(String sender, String receiver) {
		String roomId = getRoomId(sender, receiver);
		return chatRoomRepository.findByRoomId(roomId)
			.orElseGet(() -> {
				ChatRoom newRoom = new ChatRoom();
				newRoom.setRoomId(roomId);
				newRoom.setMember1(sender.compareTo(receiver) <= 0 ? sender : receiver);
				newRoom.setMember2(sender.compareTo(receiver) <= 0 ? receiver : sender);
				newRoom.setLastReadAtUser1(null);
				newRoom.setLastReadAtUser2(null);
				return chatRoomRepository.save(newRoom);
			});
	}

	// 채팅방 찾기 (없으면 새로 생성 후 메시지 저장)
	@Transactional
	public void saveMessage(ChatMessage chatMessage) {
		String roomId = getRoomId(chatMessage.getSender(), chatMessage.getReceiver());
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseGet(() -> {
				ChatRoom newRoom = new ChatRoom();
				newRoom.setRoomId(roomId);
				newRoom.setMember1(chatMessage.getSender().compareTo(chatMessage.getReceiver()) <= 0
					? chatMessage.getSender() : chatMessage.getReceiver());
				newRoom.setMember2(chatMessage.getSender().compareTo(chatMessage.getReceiver()) <= 0
					? chatMessage.getReceiver() : chatMessage.getSender());
				return chatRoomRepository.save(newRoom);
			});
		chatMessage.setTimestamp(LocalDateTime.now());
		// 대신, ChatMessage의 roomId 필드에 채팅방의 roomId를 직접 저장
		chatMessage.setRoomId(chatRoom.getRoomId());
		chatMessageRepository.save(chatMessage);
	}

	// 두 사용자의 계정 정보를 통해 채팅방 고유 식별자 생성 (예: "member1:member2")
	public String getRoomId(String sender, String receiver) {
		return (sender.compareTo(receiver) < 0) ? sender + ":" + receiver : receiver + ":" + sender;
	}

	// 채팅방 메시지 시간순 정렬 조회 (ChatMessage 엔티티의 roomId 필드로 조회)
	public List<ChatMessage> getMessages(String sender, String receiver) {
		String roomId = getRoomId(sender, receiver);
		return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
	}
}

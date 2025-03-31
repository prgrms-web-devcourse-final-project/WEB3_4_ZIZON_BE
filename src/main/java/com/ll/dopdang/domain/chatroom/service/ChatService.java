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

	//채팅방 생성 로직
	@Transactional
	public ChatRoom createChatRoom(String sender, String receiver) {
		String roomId = getRoomId(sender, receiver);
		return chatRoomRepository.findById(roomId).orElseGet(() -> {
			ChatRoom newRoom = new ChatRoom();
			newRoom.setRoomId(roomId);
			// sender와 receiver의 알파벳 순으로 client와 expert 결정
			newRoom.setClient(sender.compareTo(receiver) <= 0 ? sender : receiver);
			newRoom.setExpert(sender.compareTo(receiver) <= 0 ? receiver : sender);
			// 초기 읽음 시간은 null로 설정 (필요 시 현재 시간 등으로 설정 가능)
			newRoom.setLastReadAtUser1(null);
			newRoom.setLastReadAtUser2(null);
			return chatRoomRepository.save(newRoom);
		});
	}

	//채팅방 찾기, 채팅방이 없으면 새로운 채팅방 저장, 후 메시지 저장 로직
	@Transactional
	public void saveMessage(ChatMessage chatMessage) {
		String roomId = getRoomId(chatMessage.getSender(), chatMessage.getReceiver());
		ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseGet(() -> {
			ChatRoom newRoom = new ChatRoom();
			newRoom.setRoomId(roomId);
			newRoom.setClient(chatMessage.getSender().compareTo(chatMessage.getReceiver()) <= 0 ? chatMessage.getSender() : chatMessage.getReceiver());
			newRoom.setExpert(chatMessage.getSender().compareTo(chatMessage.getReceiver()) <= 0 ? chatMessage.getReceiver() : chatMessage.getSender());
			return chatRoomRepository.save(newRoom);
		});
		chatMessage.setTimestamp(LocalDateTime.now());
		chatMessage.setChatRoom(chatRoom);
		chatMessageRepository.save(chatMessage);
	}

	//두 사용자의 계정 정보를 통해 채팅방 생성
	public String getRoomId(String sender, String receiver) {
		return (sender.compareTo(receiver) < 0) ? sender + ":" + receiver : receiver + ":" + sender;
	}

	//채팅방 메시지 시간순 정렬
	public List<ChatMessage> getMessages(String sender, String receiver) {
		String roomId = getRoomId(sender, receiver);
		return chatMessageRepository.findByChatRoom_RoomIdOrderByTimestampAsc(roomId);
	}
}
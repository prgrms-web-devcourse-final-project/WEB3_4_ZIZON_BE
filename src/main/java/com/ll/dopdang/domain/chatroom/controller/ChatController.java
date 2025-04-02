package com.ll.dopdang.domain.chatroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ll.dopdang.domain.chatroom.dto.ChatRoomResponseDto;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;

	@PostMapping("/create")
	public ResponseEntity<ChatRoomResponseDto> createRoom(@RequestParam String sender, @RequestParam String receiver) {
		//파라미터로 로그인 사용자를(sender) 주는 이유는 웹소켓의 특성 때문
		ChatRoom chatRoom = chatService.createChatRoom(sender, receiver);
		// 기본값으로 unreadCount 0, lastMessage과 lastMessageTime은 null로 설정
		ChatRoomResponseDto responseDto = new ChatRoomResponseDto(chatRoom, sender, 0, null, null);
		return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
	}

	//채팅방 조회 시
	@GetMapping("/history")
	public ResponseEntity<List<ChatMessage>> getHistory(@RequestParam String sender, @RequestParam String receiver) {
		//파라미터로 로그인 사용자를(sender) 주는 이유는 웹소켓의 특성 때문
		List<ChatMessage> messages = chatService.getMessages(sender, receiver);
		return ResponseEntity.ok(messages);
	}

	//회원 별 채팅방
	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoom>> getChatRooms(@RequestParam String member) {
		//파라미터로 멤버를 주는 이유는 웹소켓의 특성 때문
		List<ChatRoom> rooms = chatService.getChatRoomsForUser(member);
		return ResponseEntity.ok(rooms);
	}

	//메시지 전송 시 사용되는 메시지
	// /topic/chat/이라는 stomp 구독내역에 채팅방 이름을 검색 후 있다면 보내기 또는 채팅방 생성
	@MessageMapping("/chat.send")
	public void sendMessage(ChatMessage chatMessage){
		chatService.saveMessage(chatMessage);

		String roomId = chatService.getRoomId(chatMessage.getSender(), chatMessage.getReceiver());

		messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);
	}


}
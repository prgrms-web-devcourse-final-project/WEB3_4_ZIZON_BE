package com.ll.dopdang.domain.chatroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ll.dopdang.domain.chatroom.dto.ChatRoomDetailResponse;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.service.ChatService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatController {

	private final ChatService chatService;

	//채팅방 조회 시
	@GetMapping("")
	public ResponseEntity<List<ChatRoomDetailResponse>> getHistory(@RequestParam String sender, @RequestParam String receiver) {
		//파라미터로 로그인 사용자를(sender) 주는 이유는 웹소켓의 특성 때문
		List<ChatRoomDetailResponse> messages = chatService.getChatRoomDetail(sender, receiver);
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
	public void sendMessage(ChatMessage chatMessage) {
		// 서비스 계층에서 메시지 저장 및 알림 기능 모두 처리
		chatService.saveMessage(chatMessage);
	}

	// 채팅방 읽음 처리 API
	@PostMapping("/{roomId}/read")
	public ResponseEntity<?> markAsRead(@PathVariable String roomId, @RequestParam String username) {
		try {
			ChatRoom updatedRoom = chatService.markChatRoomAsRead(roomId, username);
			return ResponseEntity.ok("채팅방 읽음 처리 완료");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("채팅방을 찾을 수 없습니다.");
		}
	}

	// 미확인 메시지 갯수 조회 API
	@GetMapping("/{roomId}/unreadCount")
	public ResponseEntity<Long> getUnreadCount(@PathVariable String roomId, @RequestParam String username) {
		long count = chatService.getUnreadCount(roomId, username);
		return ResponseEntity.ok(count);
	}
}
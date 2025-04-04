package com.ll.dopdang.domain.chatroom.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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

	/**
	 * 채팅방 상세 기록 조회
	 * 요청 시 sender, receiver를 받아 해당 채팅방의 메시지 내역을 반환합니다.
	 *
	 * @param sender   보낸 사람 (로그인 사용자)
	 * @param receiver 받은 사람
	 * @return 채팅방 상세 응답 DTO 리스트
	 */
	@GetMapping("")
	public ResponseEntity<List<ChatRoomDetailResponse>> getHistory(
		@RequestParam String sender,
		@RequestParam String receiver) {
		List<ChatRoomDetailResponse> messages = chatService.getChatRoomDetail(sender, receiver);
		return ResponseEntity.ok(messages);
	}

	/**
	 * 사용자의 채팅방 목록 조회
	 *
	 * @param member 사용자 식별자
	 * @return 사용자가 참여 중인 채팅방 리스트
	 */
	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoom>> getChatRooms(@RequestParam String member) {
		List<ChatRoom> rooms = chatService.getChatRoomsForUser(member);
		return ResponseEntity.ok(rooms);
	}

	/**
	 * WebSocket 메시지 전송 핸들러
	 *
	 * @param chatMessage 전송할 채팅 메시지
	 */
	@MessageMapping("/chat.send")
	public void sendMessage(ChatMessage chatMessage) {
		chatService.saveMessage(chatMessage);
	}

	/**
	 * 채팅방 읽음 처리 API
	 *
	 * @param roomId   채팅방 식별자
	 * @param username 읽음 처리할 사용자
	 * @return 처리 결과 메시지
	 */
	@PostMapping("/{roomId}/read")
	public ResponseEntity<?> markAsRead(@PathVariable String roomId,
		@RequestParam String username) {
		try {
			chatService.markChatRoomAsRead(roomId, username);
			return ResponseEntity.ok("채팅방 읽음 처리 완료");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("채팅방을 찾을 수 없습니다.");
		}
	}

	/**
	 * 미확인 메시지 갯수 조회 API
	 *
	 * @param roomId   채팅방 식별자
	 * @param username 사용자 식별자
	 * @return 미확인 메시지 수
	 */
	@GetMapping("/{roomId}/unreadCount")
	public ResponseEntity<Long> getUnreadCount(@PathVariable String roomId,
		@RequestParam String username) {
		long count = chatService.getUnreadCount(roomId, username);
		return ResponseEntity.ok(count);
	}
}

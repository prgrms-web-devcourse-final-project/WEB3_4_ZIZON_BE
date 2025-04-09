package com.ll.dopdang.domain.chatroom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ll.dopdang.domain.chatroom.dto.ChatRoomDetailResponse;
import com.ll.dopdang.domain.chatroom.dto.ChatRoomResponse;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.service.ChatService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
@Tag(name = "채팅 API", description = "채팅 관련 API 입니다.")
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
	@Operation(
		summary = "채팅 내역 조회",
		description = "두 사용자 간의 채팅 내역을 조회합니다."
	)
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
	@Operation(
		summary = "채팅방 목록 조회",
		description = "사용자가 참여 중인 채팅방 목록을 조회합니다."
	)
	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoomResponse>> getChatRooms(@RequestParam String member) {
		List<ChatRoomResponse> rooms = chatService.getChatRoomsForUser(member);
		return ResponseEntity.ok(rooms);
	}

	/**
	 * WebSocket 메시지 전송 핸들러
	 *
	 * @param chatMessage 전송할 채팅 메시지
	 */
	@Operation(
		summary = "메시지 전송",
		description = "지정된 채팅방에 메시지를 전송합니다."
	)
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
	@Operation(
		summary = "읽음 처리",
		description = "지정된 채팅방의 메시지들을 읽음 처리합니다."
	)
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
	@Operation(
		summary = "안 읽은 메시지 개수 조회",
		description = "지정된 채팅방에서 해당 사용자의 읽지 않은 메시지 개수를 조회합니다."
	)
	@GetMapping("/{roomId}/unreadCount")
	public ResponseEntity<Long> getUnreadCount(@PathVariable String roomId,
		@RequestParam String username) {
		long count = chatService.getUnreadCount(roomId, username);
		return ResponseEntity.ok(count);
	}

	/**
	 * 채팅방 생성 로직
	 *
	 * @param customUserDetails   현재 로그인한 사용자의 정보
	 * @param projectId project 작성자 정보를 찾기 위한 id
	 * @return void
	 */
	@Operation(
		summary = "채팅방 생성",
		description = "새 채팅방을 생성합니다. (예: 프로젝트 관련 채팅 생성)"
	)
	@PostMapping
	public ResponseEntity<String> createChatroom(@RequestParam Long projectId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		String senderEmail = customUserDetails.getMember().getEmail();

		chatService.createChatroom(senderEmail, projectId);

		return ResponseEntity.ok("채팅방이 생성되었습니다.");
	}
}

package com.ll.dopdang.domain.chatroom.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.chatroom.dto.ChatRoomDetailResponse;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.repository.ChatMessageRepository;
import com.ll.dopdang.domain.chatroom.repository.ChatRoomRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.redis.repository.RedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final RedisRepository redisRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void saveMessage(ChatMessage chatMessage) {
		String sender = chatMessage.getSender().trim().toLowerCase();
		String receiver = chatMessage.getReceiver().trim().toLowerCase();
		chatMessage.setSender(sender);
		chatMessage.setReceiver(receiver);

		String roomId = getRoomId(sender, receiver);
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseGet(() -> {
				ChatRoom newRoom = new ChatRoom();
				newRoom.setRoomId(roomId);
				// 채팅방 생성 시 sender와 receiver를 기록
				newRoom.setMember1(sender);
				newRoom.setMember2(receiver);
				return chatRoomRepository.save(newRoom);
			});
		chatMessage.setTimestamp(LocalDateTime.now());
		chatMessage.setRoomId(chatRoom.getRoomId());
		chatMessageRepository.save(chatMessage);

		// 실시간 채팅 메시지 전송
		messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);
		log.info("채팅 메시지 전송: /topic/chat/{} / sender: {} receiver: {}", roomId, sender, receiver);

		// 채팅방 목록 업데이트를 위한 알림 메시지 payload 생성
		Map<String, String> payload = new HashMap<>();
		payload.put("roomId", roomId);
		payload.put("message", "새로운 메시지가 도착했습니다.");

		// 수신자(receiver)에게 알림 전송
		messagingTemplate.convertAndSend("/topic/notice/" + receiver, payload);
		log.info("알림 전송: /topic/notice/{} payload: {}", receiver, payload);

		// 수정: 송신자(sender)에게도 알림 전송하여 채팅방 목록 갱신
		messagingTemplate.convertAndSend("/topic/notice/" + sender, payload);
		log.info("알림 전송(송신자): /topic/notice/{} payload: {}", sender, payload);
	}

	public String getRoomId(String sender, String receiver) {
		return (sender.compareTo(receiver) < 0) ? sender + ":" + receiver : receiver + ":" + sender;
	}

	@Transactional(readOnly = true)
	public List<ChatRoomDetailResponse> getChatRoomDetail(String sender, String receiver) {
		String roomId = getRoomId(sender, receiver);
		List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
		return messages.stream().map(msg -> {
			Member senderMember = memberRepository.findByEmail(msg.getSender()).orElse(null);
			String senderName = senderMember != null ? senderMember.getName() : "";
			String senderProfileImage = senderMember != null ? senderMember.getProfileImage() : "";
			// 현재 읽음 처리 로직이 잘 동작하므로 read는 true로 설정 (필요시 추가 로직 구현 가능)
			boolean read = true;
			return new ChatRoomDetailResponse(
				msg.getRoomId(),
				msg.getSender(),
				msg.getReceiver(),
				msg.getContent(),
				msg.getTimestamp(),
				senderName,
				senderProfileImage,
				read
			);
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ChatRoom> getChatRoomsForUser(String member) {
		return chatRoomRepository.findByRoomIdContaining(member);
	}

	@Transactional
	public ChatRoom markChatRoomAsRead(String roomId, String username) {
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
		LocalDateTime now = LocalDateTime.now();
		if (username.equals(chatRoom.getMember1())) {
			chatRoom.setLastReadAtUser1(now);
		} else if (username.equals(chatRoom.getMember2())) {
			chatRoom.setLastReadAtUser2(now);
		}
		ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);
		// 읽음 상태 업데이트 알림 전송
		messagingTemplate.convertAndSend("/topic/read/" + roomId,
			Map.of("roomId", roomId, "username", username, "read", true));
		return updatedRoom;
	}

	@Transactional(readOnly = true)
	public long getUnreadCount(String roomId, String username) {
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElse(null);
		if (chatRoom == null) {
			return 0;
		}
		LocalDateTime lastRead = null;
		if (username.equals(chatRoom.getMember1())) {
			lastRead = chatRoom.getLastReadAtUser1();
		} else if (username.equals(chatRoom.getMember2())) {
			lastRead = chatRoom.getLastReadAtUser2();
		}
		List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
		final LocalDateTime finalLastRead = lastRead;
		return messages.stream()
			.filter(msg -> !msg.getSender().equals(username))
			.filter(msg -> finalLastRead == null || msg.getTimestamp().isAfter(finalLastRead))
			.count();
	}
}

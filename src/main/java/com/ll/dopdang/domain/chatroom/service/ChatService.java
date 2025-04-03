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
				// 엔티티에 맞게 채팅방의 양쪽 회원을 설정
				newRoom.setMember1(sender);
				newRoom.setMember2(receiver);
				return chatRoomRepository.save(newRoom);
			});
		chatMessage.setTimestamp(LocalDateTime.now());
		chatMessage.setRoomId(chatRoom.getRoomId());
		chatMessageRepository.save(chatMessage);

		messagingTemplate.convertAndSend("/topic/chat/" + roomId, chatMessage);
		log.info("메시지 전송: /topic/chat/{} / sender: {} receiver: {}", roomId, sender, receiver);

		if (!sender.equals(receiver)) {
			Map<String, String> payload = new HashMap<>();
			payload.put("roomId", roomId);
			payload.put("message", "새로운 메시지가 도착했습니다.");
			messagingTemplate.convertAndSend("/topic/notice/" + receiver, payload);
			log.info("알림 전송: /topic/notice/{} payload: {}", receiver, payload);
		}
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
			// read 여부는 메시지의 타임스탬프와 마지막 읽은 시간 비교 등 로직을 추가하여 설정
			boolean read = true;
			// 상대방 메시지인 경우 읽음 여부 계산 (예시)
			// 실제로는 현재 사용자의 마지막 읽은 시간과 비교하는 로직이 필요합니다.
			return new ChatRoomDetailResponse(
				msg.getRoomId(),
				msg.getSender(),
				msg.getReceiver(),
				msg.getContent(),
				msg.getTimestamp(),
				senderName,
				senderProfileImage,
				read   // 추가된 read 필드
			);
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ChatRoom> getChatRoomsForUser(String member) {
		return chatRoomRepository.findByRoomIdContaining(member);
	}

	// 읽음 처리 기능: 특정 채팅방을 지정 사용자가 읽었음을 처리하는 메서드
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
		// 읽음 처리 후 실시간 업데이트 이벤트 전송
		messagingTemplate.convertAndSend("/topic/read/" + roomId,
			Map.of("roomId", roomId, "username", username, "read", true));
		return updatedRoom;
	}

	// 읽지 않은 메시지 개수를 계산하는 메서드 (사용자 기준)
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

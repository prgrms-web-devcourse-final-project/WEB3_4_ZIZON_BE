package com.ll.dopdang.domain.chatroom.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.chatroom.dto.ChatRoomDetailResponse;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.repository.ChatMessageRepository;
import com.ll.dopdang.domain.chatroom.repository.ChatRoomRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final MemberRepository memberRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final int RECENT_MESSAGE_LIMIT = 100;
	private static final String UNREAD_COUNT_KEY_TEMPLATE = "chat:%s:unread:%s";

	/**
	 * 채팅 메시지를 저장합니다.
	 * DB에 저장 후 Redis 캐시에 추가하고, 실시간 브로드캐스트를 위해 메시지를 전송합니다.
	 *
	 * @param chatMessage 저장할 채팅 메시지
	 */
	@Transactional
	public void saveMessage(ChatMessage chatMessage) {
		// sender, receiver 소문자 처리 및 타임스탬프 세팅
		chatMessage.setSender(chatMessage.getSender().trim().toLowerCase());
		chatMessage.setReceiver(chatMessage.getReceiver().trim().toLowerCase());
		chatMessage.setTimestamp(LocalDateTime.now());

		// 채팅방 찾기 또는 새로 생성
		String roomId = getRoomId(chatMessage.getSender(), chatMessage.getReceiver());
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseGet(() -> {
				ChatRoom newRoom = new ChatRoom();
				newRoom.setRoomId(roomId);
				newRoom.setMember1(chatMessage.getSender());
				newRoom.setMember2(chatMessage.getReceiver());
				return chatRoomRepository.save(newRoom);
			});

		chatMessage.setRoomId(chatRoom.getRoomId());
		chatMessageRepository.save(chatMessage);

		// Redis 캐시에 메시지 추가 (오른쪽에 추가)
		String redisKey = "chat:" + chatRoom.getRoomId() + ":messages";
		redisTemplate.opsForList().rightPush(redisKey, chatMessage);
		long listSize = redisTemplate.opsForList().size(redisKey);
		if (listSize > RECENT_MESSAGE_LIMIT) {
			redisTemplate.opsForList().trim(redisKey, listSize - RECENT_MESSAGE_LIMIT, -1);
		}

		// 채팅방 캐시 업데이트 (Hash 방식을 사용하여 중복 생성 방지)
		updateChatRoomCache(chatRoom.getRoomId(), chatMessage);

		// 실시간 브로드캐스트 (예시: WebSocket을 이용)
		messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getRoomId(), chatMessage);
	}

	/**
	 * 채팅방의 메시지 기록을 조회합니다.
	 * Redis 캐시에 저장된 데이터가 있으면 전체 리스트를 한꺼번에 변환하여 반환하고,
	 * 없으면 DB에서 조회합니다.
	 *
	 * @param roomId 채팅방 식별자
	 * @return 채팅 메시지 리스트
	 */
	@Transactional(readOnly = true)
	public List<ChatMessage> getChatRoomDetailByRoomId(String roomId) {
		String redisKey = "chat:" + roomId + ":messages";
		List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
		if (cachedMessages != null && !cachedMessages.isEmpty()) {
			// Redis에 저장된 전체 메시지 리스트(예: JSON 배열 형식)를 한 번에 List<ChatMessage>로 변환
			return objectMapper.convertValue(cachedMessages, new TypeReference<List<ChatMessage>>() {});
		} else {
			return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
		}
	}

	/**
	 * sender, receiver 정보를 이용하여 채팅방의 상세 응답 DTO 리스트를 생성합니다.
	 *
	 * @param sender   채팅을 보낸 사람
	 * @param receiver 채팅을 받은 사람
	 * @return 채팅방 상세 응답 DTO 리스트
	 */
	@Transactional(readOnly = true)
	public List<ChatRoomDetailResponse> getChatRoomDetail(String sender, String receiver) {
		String roomId = getRoomId(sender.trim().toLowerCase(), receiver.trim().toLowerCase());
		List<ChatMessage> messages = getChatRoomDetailByRoomId(roomId);
		List<ChatRoomDetailResponse> responseList = new ArrayList<>();

		// 로컬 캐시(Map)을 사용하여 중복된 회원 조회 방지
		Map<String, Member> memberCache = new HashMap<>();

		for (ChatMessage msg : messages) {
			// sender 이메일에 대해 캐시가 없으면 DB에서 조회하여 캐시에 저장
			Member senderMember = memberCache.computeIfAbsent(msg.getSender(), email ->
				memberRepository.findByEmail(email).orElse(null)
			);
			String senderName = senderMember != null ? senderMember.getName() : "";
			String senderProfileImage = senderMember != null ? senderMember.getProfileImage() : "";
			responseList.add(new ChatRoomDetailResponse(
				msg.getRoomId(),
				msg.getSender(),
				msg.getReceiver(),
				msg.getContent(),
				msg.getTimestamp(),
				senderName,
				senderProfileImage,
				true // 기본 읽음 여부 설정(필요시 변경)
			));
		}
		return responseList;
	}

	/**
	 * 사용자별 채팅방 목록을 조회합니다.
	 * Redis에 캐시된 목록이 있으면 사용하고, 없으면 DB에서 조회 후 캐싱합니다.
	 *
	 * @param member 사용자 식별자
	 * @return 사용자가 참여 중인 채팅방 리스트
	 */
	@Transactional(readOnly = true)
	public List<ChatRoom> getChatRoomsForUser(String member) {
		String redisKey = "chatrooms:" + member;
		// Hash를 사용하여 roomId를 필드로 저장
		Map<Object, Object> roomSummaries = redisTemplate.opsForHash().entries(redisKey);
		List<ChatRoom> rooms = new ArrayList<>();
		if (roomSummaries != null && !roomSummaries.isEmpty()) {
			for (Object value : roomSummaries.values()) {
				String summaryJson = (String) value;
				// JSON 문자열에서 roomId 추출 (정규식은 상황에 맞게 수정)
				String roomId = summaryJson.replaceAll(".*\"roomId\":\"([^\"]+)\".*", "$1");
				chatRoomRepository.findByRoomId(roomId).ifPresent(rooms::add);
			}
			return rooms;
		}
		// 캐시가 없으면 DB에서 조회 후 Redis에 캐싱 (채팅방 요약정보는 개별 로직에 따라 구현)
		List<ChatRoom> dbRooms = chatRoomRepository.findByMember(member);
		for (ChatRoom room : dbRooms) {
			String roomSummary = createRoomSummaryJson(room.getRoomId(), null);
			redisTemplate.opsForHash().put(redisKey, room.getRoomId(), roomSummary);
		}
		return dbRooms;
	}

	/**
	 * 채팅방 캐시 업데이트 (Hash 방식)
	 * 각 사용자의 "chatrooms:{username}" 해시에 roomId를 필드로 저장하여 중복 생성을 방지합니다.
	 *
	 * @param roomId      채팅방 식별자
	 * @param chatMessage 최신 채팅 메시지 (null이면 업데이트하지 않음)
	 */
	private void updateChatRoomCache(String roomId, ChatMessage chatMessage) {
		if (chatMessage == null) return;
		String keySender = "chatrooms:" + chatMessage.getSender();
		String keyReceiver = "chatrooms:" + chatMessage.getReceiver();
		String roomSummary = createRoomSummaryJson(roomId, chatMessage);
		// 각 채팅방은 roomId 필드로 하나만 저장됨 (Hash 사용)
		redisTemplate.opsForHash().put(keySender, roomId, roomSummary);
		redisTemplate.opsForHash().put(keyReceiver, roomId, roomSummary);
	}

	/**
	 * 채팅방 요약 정보 JSON 문자열 생성
	 * chatMessage가 null이면 기본 형식(채팅방 아이디만 포함)으로 생성합니다.
	 *
	 * @param roomId      채팅방 식별자
	 * @param chatMessage 최신 메시지 (없으면 null)
	 * @return 채팅방 요약 JSON 문자열
	 */
	private String createRoomSummaryJson(String roomId, ChatMessage chatMessage) {
		if (chatMessage != null) {
			return "{\"roomId\":\"" + roomId + "\"," +
				"\"lastMessage\":\"" + chatMessage.getContent() + "\"," +
				"\"timestamp\":\"" + chatMessage.getTimestamp() + "\"}";
		} else {
			return "{\"roomId\":\"" + roomId + "\"}";
		}
	}

	/**
	 * sender와 receiver를 기반으로 채팅방 ID 생성
	 *
	 * @param sender   보낸 사람
	 * @param receiver 받은 사람
	 * @return 채팅방 ID 문자열
	 */
	public String getRoomId(String sender, String receiver) {
		return (sender.compareTo(receiver) < 0) ? sender + ":" + receiver : receiver + ":" + sender;
	}

	/**
	 * 채팅방 읽음 처리
	 *
	 * @param roomId   채팅방 식별자
	 * @param username 읽음 처리할 사용자
	 * @return 업데이트된 채팅방 엔티티
	 */
	@Transactional
	public ChatRoom markChatRoomAsRead(String roomId, String username) {
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
		LocalDateTime now = LocalDateTime.now();
		if (username.equals(chatRoom.getMember1())) {
			chatRoom.setLastReadAtUser1(now);
		} else if (username.equals(chatRoom.getMember2())) {
			chatRoom.setLastReadAtUser2(now);
		}
		ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);
		String unreadKey = String.format(UNREAD_COUNT_KEY_TEMPLATE, roomId, username);
		redisTemplate.opsForValue().set(unreadKey, 0);
		return updatedRoom;
	}

	/**
	 * 사용자의 미확인 메시지 수 조회
	 *
	 * @param roomId   채팅방 식별자
	 * @param username 사용자 식별자
	 * @return 미확인 메시지 수
	 */
	@Transactional(readOnly = true)
	public long getUnreadCount(String roomId, String username) {
		String unreadKey = String.format(UNREAD_COUNT_KEY_TEMPLATE, roomId, username);
		Object value = redisTemplate.opsForValue().get(unreadKey);
		if (value == null) {
			return 0;
		}
		try {
			return Long.parseLong(value.toString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}

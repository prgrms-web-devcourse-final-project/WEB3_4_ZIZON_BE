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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final MemberRepository memberRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	private static final int RECENT_MESSAGE_LIMIT = 100;
	private static final String UNREAD_COUNT_KEY_TEMPLATE = "chat:%s:unread:%s";
	private static final String CHAT_MESSAGES_KEY_TEMPLATE = "chat:%s:messages";
	private static final String CHAT_ROOMS_KEY_TEMPLATE = "chatrooms:%s";
	private static final String CHAT_ROOM_TIMESTAMP_KEY_TEMPLATE = "chatrooms:%s:timestamp";

	// 메시지 남아있는 채팅방 추적용 Set
	private static final String ACTIVE_ROOMS_KEY = "chat:active_rooms";

	// Redis에 저장된 데이터의 만료 시간 (30분)
	private static final long CACHE_EXPIRATION = 30;

	/**
	 * 채팅 메시지를 저장합니다.
	 * 원래 DB에 바로 저장하는 대신 Redis 캐시에만 저장하도록 수정하였습니다.
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

		// 기존에는 여기서 DB에 저장했지만,
		// 지금은 새 메시지는 Redis 캐시에만 저장하여 나중에 배치로 DB에 저장하도록 합니다.
		// chatMessageRepository.save(chatMessage);

		// Redis 캐시에 메시지 추가 (오른쪽에 추가)
		String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, chatRoom.getRoomId());
		redisTemplate.opsForList().rightPush(redisKey, chatMessage);
		redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);

		// 활성 채팅방 목록에 추가
		redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, chatRoom.getRoomId());

		// 메시지 수가 제한을 초과하면 오래된 메시지 제거
		long listSize = redisTemplate.opsForList().size(redisKey);
		if (listSize > RECENT_MESSAGE_LIMIT) {
			redisTemplate.opsForList().trim(redisKey, listSize - RECENT_MESSAGE_LIMIT, -1);
		}

		// 채팅방 캐시 업데이트 (Hash 방식을 사용하여 중복 생성 방지)
		updateChatRoomCache(chatRoom.getRoomId(), chatMessage);

		// 채팅방 타임스탬프 업데이트 (메시지 전송 시간으로)
		updateChatRoomTimestamp(chatRoom.getRoomId(), chatMessage.getTimestamp());

		// 실시간 브로드캐스트 (WebSocket 이용)
		messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getRoomId(), chatMessage);

		// 상대방의 읽지 않은 메시지 수 증가
		String unreadKey = String.format(UNREAD_COUNT_KEY_TEMPLATE, roomId, chatMessage.getReceiver());
		Object value = redisTemplate.opsForValue().get(unreadKey);
		long currentUnread = 0;
		if (value != null) {
			try {
				currentUnread = Long.parseLong(value.toString());
			} catch (NumberFormatException e) {
				log.error("Failed to parse unread count: {}", e.getMessage());
			}
		}
		redisTemplate.opsForValue().set(unreadKey, currentUnread + 1);
	}

	/**
	 * 채팅방 타임스탬프 정보를 업데이트합니다.
	 *
	 * @param roomId 채팅방 ID
	 * @param timestamp 최신 메시지 타임스탬프
	 */
	private void updateChatRoomTimestamp(String roomId, LocalDateTime timestamp) {
		String timeKey = String.format(CHAT_ROOM_TIMESTAMP_KEY_TEMPLATE, roomId);
		String timestampStr = timestamp.toString();
		redisTemplate.opsForValue().set(timeKey, timestampStr);
		redisTemplate.expire(timeKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
	}

	/**
	 * 채팅방의 메시지 기록을 조회합니다.
	 * Redis 캐시에 저장된 데이터가 있으면 캐시에서 조회하고,
	 * 없으면 DB에서 조회한 후 Redis에 캐싱합니다.
	 *
	 * @param roomId 채팅방 식별자
	 * @return 채팅 메시지 리스트
	 */
	@Transactional(readOnly = true)
	public List<ChatMessage> getChatRoomDetailByRoomId(String roomId) {
		String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, roomId);
		List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);

		if (cachedMessages != null && !cachedMessages.isEmpty()) {
			log.debug("Redis에서 채팅 메시지를 가져옵니다. roomId: {}", roomId);

			LocalDateTime latestDbMessageTime = chatMessageRepository.findLatestMessageTimeByRoomId(roomId);
			List<ChatMessage> redisMessages = objectMapper.convertValue(cachedMessages, new TypeReference<List<ChatMessage>>() {});
			LocalDateTime latestRedisMessageTime = redisMessages.isEmpty() ? null :
				redisMessages.get(redisMessages.size() - 1).getTimestamp();

			if (latestDbMessageTime != null && latestRedisMessageTime != null &&
				!latestDbMessageTime.isEqual(latestRedisMessageTime)) {
				log.debug("Redis 캐시와 DB 데이터가 다릅니다. DB에서 최신 데이터를 가져옵니다.");
				List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);

				redisTemplate.delete(redisKey);
				for (ChatMessage message : messages) {
					redisTemplate.opsForList().rightPush(redisKey, message);
				}
				redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);

				return messages;
			}

			return redisMessages;
		} else {
			log.debug("DB에서 채팅 메시지를 가져와 Redis에 저장합니다. roomId: {}", roomId);
			List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);

			if (!messages.isEmpty()) {
				for (ChatMessage message : messages) {
					redisTemplate.opsForList().rightPush(redisKey, message);
				}
				redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);

				if (!messages.isEmpty()) {
					redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, roomId);
					ChatMessage latestMessage = messages.get(messages.size() - 1);
					updateChatRoomTimestamp(roomId, latestMessage.getTimestamp());
				}
			}

			return messages;
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

		Map<String, Member> memberCache = new HashMap<>();

		for (ChatMessage msg : messages) {
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
				true
			));
		}
		return responseList;
	}

	/**
	 * 사용자의 채팅방 목록을 조회합니다.
	 * Redis와 DB 모두 확인하여 동기화된 최신 데이터를 제공합니다.
	 *
	 * @param member 사용자 식별자
	 * @return 사용자가 참여 중인 채팅방 리스트
	 */
	@Transactional(readOnly = true)
	public List<ChatRoom> getChatRoomsForUser(String member) {
		String redisKey = String.format(CHAT_ROOMS_KEY_TEMPLATE, member);

		List<ChatRoom> dbRooms = chatRoomRepository.findByMember(member);
		Set<String> dbRoomIds = dbRooms.stream()
			.map(ChatRoom::getRoomId)
			.collect(Collectors.toSet());

		Map<Object, Object> roomSummaries = redisTemplate.opsForHash().entries(redisKey);
		Set<String> redisRoomIds = new HashSet<>();

		if (!roomSummaries.isEmpty()) {
			redisRoomIds = roomSummaries.keySet().stream()
				.map(Object::toString)
				.collect(Collectors.toSet());
		}

		boolean needsSync = false;

		for (String roomId : dbRoomIds) {
			if (!redisRoomIds.contains(roomId)) {
				needsSync = true;
				break;
			}
		}

		for (String roomId : redisRoomIds) {
			if (!dbRoomIds.contains(roomId)) {
				needsSync = true;
				break;
			}
		}

		if (needsSync || roomSummaries.isEmpty()) {
			log.debug("DB와 Redis 채팅방 목록이 일치하지 않아 동기화합니다. member: {}", member);

			redisTemplate.delete(redisKey);

			for (ChatRoom room : dbRooms) {
				String roomId = room.getRoomId();
				List<ChatMessage> messages = chatMessageRepository.findTopMessagesByRoomIdOrderByTimestampDesc(roomId, 1);
				ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);

				String roomSummary = createRoomSummaryJson(roomId, lastMessage);
				redisTemplate.opsForHash().put(redisKey, roomId, roomSummary);

				if (lastMessage != null) {
					updateChatRoomTimestamp(roomId, lastMessage.getTimestamp());
				}
			}

			redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
		} else {
			log.debug("Redis에서 채팅방 목록을 가져옵니다. member: {}", member);
		}

		return dbRooms;
	}

	/**
	 * JSON 문자열에서 roomId 추출
	 */
	private String extractRoomIdFromJson(String json) {
		try {
			if (json == null || json.isEmpty()) {
				return null;
			}
			return json.replaceAll(".*\"roomId\":\"([^\"]+)\".*", "$1");
		} catch (Exception e) {
			log.error("Failed to extract roomId from JSON: {}", e.getMessage());
			return null;
		}
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

		String keySender = String.format(CHAT_ROOMS_KEY_TEMPLATE, chatMessage.getSender());
		String keyReceiver = String.format(CHAT_ROOMS_KEY_TEMPLATE, chatMessage.getReceiver());
		String roomSummary = createRoomSummaryJson(roomId, chatMessage);

		redisTemplate.opsForHash().put(keySender, roomId, roomSummary);
		redisTemplate.expire(keySender, CACHE_EXPIRATION, TimeUnit.MINUTES);

		redisTemplate.opsForHash().put(keyReceiver, roomId, roomSummary);
		redisTemplate.expire(keyReceiver, CACHE_EXPIRATION, TimeUnit.MINUTES);
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
			String content = chatMessage.getContent()
				.replace("\"", "\\\"")
				.replace("\n", "\\n");

			return "{\"roomId\":\"" + roomId + "\"," +
				"\"lastMessage\":\"" + content + "\"," +
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
	 */
	@Transactional
	public void markChatRoomAsRead(String roomId, String username) {
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
		LocalDateTime now = LocalDateTime.now();

		if (username.equals(chatRoom.getMember1())) {
			chatRoom.setLastReadAtUser1(now);
		} else if (username.equals(chatRoom.getMember2())) {
			chatRoom.setLastReadAtUser2(now);
		}

		String unreadKey = String.format(UNREAD_COUNT_KEY_TEMPLATE, roomId, username);
		redisTemplate.opsForValue().set(unreadKey, 0);

		messagingTemplate.convertAndSend("/topic/read/" + roomId,
			"{\"roomId\":\"" + roomId + "\",\"reader\":\"" + username + "\",\"timestamp\":\"" + now + "\"}");
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
			log.error("Failed to parse unread count: {}", e.getMessage());
			return 0;
		}
	}

	/**
	 * 30분마다 Redis의 채팅 메시지를 DB에 저장하고 Redis 캐시를 정리합니다.
	 */
	@Scheduled(fixedRate = 60 * 1000) // 30분마다 실행
	@Transactional
	public void flushChatMessagesToDB() {
		log.info("Redis 채팅 메시지를 DB에 저장하는 스케줄러 실행");

		Set<Object> activeRoomIds = redisTemplate.opsForSet().members(ACTIVE_ROOMS_KEY);
		if (activeRoomIds == null || activeRoomIds.isEmpty()) {
			log.info("활성 채팅방이 없습니다.");
			return;
		}

		for (Object roomIdObj : activeRoomIds) {
			String roomId = (String) roomIdObj;
			String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, roomId);

			List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
			if (cachedMessages == null || cachedMessages.isEmpty()) {
				continue;
			}

			List<ChatMessage> messages = objectMapper.convertValue(
				cachedMessages, new TypeReference<List<ChatMessage>>() {});

			// DB에 저장되지 않은 새 메시지들만 필터링 (ID가 null인 경우)
			List<ChatMessage> newMessages = messages.stream()
				.filter(msg -> msg.getId() == null)
				.collect(Collectors.toList());

			if (!newMessages.isEmpty()) {
				log.info("채팅방 {}의 {} 개 메시지를 DB에 저장합니다.", roomId, newMessages.size());
				chatMessageRepository.saveAll(newMessages);
			}

			redisTemplate.delete(redisKey);
		}

		redisTemplate.delete(ACTIVE_ROOMS_KEY);

		log.info("Redis 채팅 메시지를 DB에 저장 완료");
	}
}

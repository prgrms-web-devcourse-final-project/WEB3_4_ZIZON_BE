package com.ll.dopdang.domain.chatroom.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.chatroom.dto.ChatRoomDetailResponse;
import com.ll.dopdang.domain.chatroom.dto.ChatRoomResponse;
import com.ll.dopdang.domain.chatroom.dto.NotificationPayload;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.repository.ChatMessageRepository;
import com.ll.dopdang.domain.chatroom.repository.ChatRoomRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.project.dto.ProjectDetailResponse;
import com.ll.dopdang.domain.project.service.ProjectService;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

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
	private final ProjectService projectService;
	private final ObjectMapper objectMapper;

	private static final int RECENT_MESSAGE_LIMIT = 100;
	private static final String UNREAD_COUNT_KEY_TEMPLATE = "chat:%s:unread:%s";
	private static final String CHAT_MESSAGES_KEY_TEMPLATE = "chat:%s:messages";
	private static final String CHAT_ROOMS_KEY_TEMPLATE = "chatrooms:%s";
	private static final String CHAT_ROOM_TIMESTAMP_KEY_TEMPLATE = "chatrooms:%s:timestamp";
	private static final String ACTIVE_ROOMS_KEY = "chat:active_rooms";
	private static final long CACHE_EXPIRATION = 30;
	private static final long LOCK_EXPIRATION = 10;

	/**
	 * 프로젝트 ID, sender, receiver를 기반으로 채팅방 ID를 생성합니다.
	 * 예: projectId = 123, sender="alice@x.com", receiver="bob@y.com"
	 *     -> "123|alice@x.com:bob@y.com" (사전순 정렬)
	 */
	public String getRoomId(String sender, String receiver, Long projectId) {
		String sortedParticipants = (sender.compareTo(receiver) < 0)
			? sender + ":" + receiver
			: receiver + ":" + sender;
		return projectId + "|" + sortedParticipants;
	}

	/**
	 * 메시지를 저장합니다.
	 */
	@Transactional
	public void saveMessage(ChatMessage chatMessage) {
		// sender, receiver 소문자 처리 및 타임스탬프 세팅
		chatMessage.setSender(chatMessage.getSender().trim().toLowerCase());
		chatMessage.setReceiver(chatMessage.getReceiver().trim().toLowerCase());
		chatMessage.setTimestamp(LocalDateTime.now());

		// 프로젝트 ID가 반드시 포함되어야 함
		Long projectId = chatMessage.getProjectId();
		if (projectId == null) {
			throw new IllegalArgumentException("프로젝트 ID가 누락되었습니다.");
		}

		// 수정된 getRoomId 메소드로 채팅방 ID 생성
		String roomId = getRoomId(chatMessage.getSender(), chatMessage.getReceiver(), projectId);
		// 채팅방 조회 또는 생성
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseGet(() -> {
				ChatRoom newRoom = new ChatRoom();
				newRoom.setRoomId(roomId);
				newRoom.setMember1(chatMessage.getSender());
				newRoom.setMember2(chatMessage.getReceiver());
				newRoom.setProjectId(projectId);
				return chatRoomRepository.save(newRoom);
			});
		chatMessage.setRoomId(chatRoom.getRoomId());
		chatMessage.setChatRoom(chatRoom);

		// Redis 캐시에 메시지 추가 (오른쪽에 추가)
		String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, chatRoom.getRoomId());
		redisTemplate.opsForList().rightPush(redisKey, chatMessage);
		redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);

		// 활성 채팅방 목록에 추가
		redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, chatRoom.getRoomId());

		// 메시지 개수가 제한 초과 시 오래된 메시지 제거
		long listSize = redisTemplate.opsForList().size(redisKey);
		if (listSize > RECENT_MESSAGE_LIMIT) {
			redisTemplate.opsForList().trim(redisKey, listSize - RECENT_MESSAGE_LIMIT, -1);
		}

		// 채팅방 캐시 업데이트 (Hash 방식)
		updateChatRoomCache(chatRoom.getRoomId(), chatMessage);

		// 채팅방 타임스탬프 업데이트
		updateChatRoomTimestamp(chatRoom.getRoomId(), chatMessage.getTimestamp());

		// 실시간 브로드캐스트 (WebSocket)
		messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getRoomId(), chatMessage);

		// --- 미읽은 메시지 알림 처리 ---
		String activeRoomKey = "active_chat_room:" + chatMessage.getReceiver();
		Object activeRoomObj = redisTemplate.opsForValue().get(activeRoomKey);
		if (activeRoomObj == null || !activeRoomObj.toString().equals(chatRoom.getRoomId())) {
			String unreadKey = String.format(UNREAD_COUNT_KEY_TEMPLATE, roomId, chatMessage.getReceiver());
			Object value = redisTemplate.opsForValue().get(unreadKey);
			long currentUnread = (value != null) ? Long.parseLong(value.toString()) : 0;
			long updatedUnread = currentUnread + 1;
			redisTemplate.opsForValue().set(unreadKey, updatedUnread);

			NotificationPayload notification = new NotificationPayload(
				chatRoom.getRoomId(),
				chatMessage.getSender(),
				chatMessage.getContent(),
				(int) updatedUnread
			);
			messagingTemplate.convertAndSend("/topic/notice/" + chatMessage.getReceiver(), notification);
		}
	}

	/**
	 * 채팅방 타임스탬프 업데이트
	 */
	private void updateChatRoomTimestamp(String roomId, LocalDateTime timestamp) {
		String timeKey = String.format(CHAT_ROOM_TIMESTAMP_KEY_TEMPLATE, roomId);
		redisTemplate.opsForValue().set(timeKey, timestamp.toString());
		redisTemplate.expire(timeKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
	}

	/**
	 * Redis 또는 DB에서 채팅방 메시지 기록 조회
	 */
	@Transactional(readOnly = true)
	public List<ChatMessage> getChatRoomDetailByRoomId(String roomId) {
		String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, roomId);
		List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
		if (cachedMessages != null && !cachedMessages.isEmpty()) {
			log.debug("Redis에서 채팅 메시지를 가져옵니다. roomId: {}", roomId);
			LocalDateTime latestDbMessageTime = chatMessageRepository.findLatestMessageTimeByRoomId(roomId);
			List<ChatMessage> redisMessages = objectMapper.convertValue(cachedMessages,
				new TypeReference<List<ChatMessage>>() {});
			LocalDateTime latestRedisMessageTime = redisMessages.isEmpty() ? null :
				redisMessages.get(redisMessages.size() - 1).getTimestamp();
			if (latestDbMessageTime == null || (latestRedisMessageTime != null
				&& latestRedisMessageTime.isAfter(latestDbMessageTime))) {
				return redisMessages;
			}
			if (latestRedisMessageTime != null && !latestDbMessageTime.isEqual(latestRedisMessageTime)) {
				log.debug("Redis와 DB 데이터가 다릅니다. DB에서 최신 데이터를 조회합니다.");
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
				redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, roomId);
				ChatMessage latestMessage = messages.get(messages.size() - 1);
				updateChatRoomTimestamp(roomId, latestMessage.getTimestamp());
			}
			return messages;
		}
	}

	/**
	 * sender, receiver를 통해 채팅방 상세 정보를 DTO로 반환
	 */
	@Transactional(readOnly = true)
	public List<ChatRoomDetailResponse> getChatRoomDetail(String sender, String receiver, Long projectId) {
		String roomId = getRoomId(sender.trim().toLowerCase(), receiver.trim().toLowerCase(), projectId);
		List<ChatMessage> messages = getChatRoomDetailByRoomId(roomId);
		Map<String, Member> memberCache = new HashMap<>();
		List<ChatRoomDetailResponse> responseList = new ArrayList<>();
		for (ChatMessage msg : messages) {
			Member senderMember = memberCache.computeIfAbsent(msg.getSender(), email ->
				memberRepository.findByEmail(email).orElse(null)
			);
			ChatRoomDetailResponse detailDto = ChatRoomDetailResponse.from(msg, senderMember, true);
			responseList.add(detailDto);
		}
		return responseList;
	}

	/**
	 * 사용자의 채팅방 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<ChatRoomResponse> getChatRoomsForUser(String member, Long currentUserId) {
		String redisKey = String.format(CHAT_ROOMS_KEY_TEMPLATE, member);
		// Redis에서 해당 사용자의 채팅방 정보를 해시로 가져옴
		Map<Object, Object> redisEntries = redisTemplate.opsForHash().entries(redisKey);
		List<ChatRoomResponse> dtoList = new ArrayList<>();

		if (!redisEntries.isEmpty()) {
			log.debug("Redis에서 채팅방 목록을 가져옵니다. member: {}", member);
			// Redis에 저장된 값은 이미 채워진 ChatRoomResponse의 JSON 문자열이라고 가정
			for (Map.Entry<Object, Object> entry : redisEntries.entrySet()) {
				try {
					ChatRoomResponse response = objectMapper.readValue((String) entry.getValue(), ChatRoomResponse.class);
					dtoList.add(response);
				} catch (Exception e) {
					log.error("채팅방 응답 파싱 오류: {}", e.getMessage());
				}
			}
			// 최신 메시지 시간 기준 내림차순 정렬
			dtoList.sort((a, b) -> {
				if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
				if (a.getLastMessageTime() == null) return 1;
				if (b.getLastMessageTime() == null) return -1;
				return b.getLastMessageTime().compareTo(a.getLastMessageTime());
			});
		} else {
			log.debug("Redis 캐시 없음. DB에서 채팅방 목록 조회. member: {}", member);
			List<ChatRoom> dbRooms = chatRoomRepository.findByMember(member);
			for (ChatRoom room : dbRooms) {
				String otherEmail = room.getMember1().equalsIgnoreCase(member) ? room.getMember2() : room.getMember1();
				// DB에서 해당 채팅방의 마지막 메시지 조회
				List<ChatMessage> messages = chatMessageRepository.findTopMessagesByRoomIdOrderByTimestampDesc(room.getRoomId(), 1);
				ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);
				int unreadCount = (int) getUnreadCount(room.getRoomId(), member);
				Member currentMember = memberRepository.findByEmail(member)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 없음"));
				Member otherUser = memberRepository.findByEmail(otherEmail).orElse(null);

				// 완전한 정보를 채워서 ChatRoomResponse 생성
				ChatRoomResponse response = ChatRoomResponse.from(room, currentMember, otherUser, lastMessage, unreadCount);
				dtoList.add(response);

				try {
					// 완전한 ChatRoomResponse 객체를 JSON 문자열로 변환하여 Redis에 저장
					String json = objectMapper.writeValueAsString(response);
					redisTemplate.opsForHash().put(redisKey, room.getRoomId(), json);
				} catch (Exception e) {
					log.error("Redis 캐시 저장 오류: {}", e.getMessage());
				}
			}
			redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);

			// 내림차순 정렬
			dtoList.sort((a, b) -> {
				if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
				if (a.getLastMessageTime() == null) return 1;
				if (b.getLastMessageTime() == null) return -1;
				return b.getLastMessageTime().compareTo(a.getLastMessageTime());
			});
		}
		return dtoList;
	}

	/**
	 * 채팅방 캐시 업데이트 (각 사용자 Redis 해시 갱신)
	 */
	private void updateChatRoomCache(String roomId, ChatMessage chatMessage) {
		if (chatMessage == null) return;
		ChatRoom room = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new IllegalStateException("채팅방 없음"));
		Long projectId = room.getProjectId();
		String keySender = String.format(CHAT_ROOMS_KEY_TEMPLATE, chatMessage.getSender());
		String keyReceiver = String.format(CHAT_ROOMS_KEY_TEMPLATE, chatMessage.getReceiver());
		String roomSummary = createRoomSummaryJson(roomId, chatMessage, projectId);
		redisTemplate.opsForHash().put(keySender, roomId, roomSummary);
		redisTemplate.expire(keySender, CACHE_EXPIRATION, TimeUnit.MINUTES);
		redisTemplate.opsForHash().put(keyReceiver, roomId, roomSummary);
		redisTemplate.expire(keyReceiver, CACHE_EXPIRATION, TimeUnit.MINUTES);
	}

	/**
	 * 채팅방 요약 JSON 문자열 생성 (기본 버전)
	 */
	private String createRoomSummaryJson(String roomId, ChatMessage chatMessage, Long projectId) {
		if (chatMessage != null) {
			String content = chatMessage.getContent().replace("\"", "\\\"").replace("\n", "\\n");
			return "{\"roomId\":\"" + roomId + "\"," +
				"\"lastMessage\":\"" + content + "\"," +
				"\"timestamp\":\"" + chatMessage.getTimestamp() + "\"," +
				"\"projectId\":" + projectId + "}";
		} else {
			return "{\"roomId\":\"" + roomId + "\"," +
				"\"projectId\":" + projectId + "}";
		}
	}

	/**
	 * sender와 receiver, projectId를 기반으로 채팅방 ID 생성 (createChatroom 호출용)
	 */
	public void createChatroom(String email, Long projectId) {
		ProjectDetailResponse projectDetail = projectService.getProjectById(projectId);
		String receiverEmail = projectDetail.getEmails().trim().toLowerCase();
		email = email.trim().toLowerCase();

		if (email.equals(receiverEmail)) {
			throw new ServiceException(ErrorCode.CHATTING_EQUALS_EMAIL);
		}

		// 프로젝트 ID를 포함하여 채팅방 ID 생성
		String roomId = getRoomId(email, receiverEmail, projectId);
		Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByRoomId(roomId);
		ChatRoom chatRoom;
		if (existingChatRoom.isPresent()) {
			chatRoom = existingChatRoom.get();
		} else {
			chatRoom = new ChatRoom();
			chatRoom.setRoomId(roomId);
			chatRoom.setMember1(email);
			chatRoom.setMember2(receiverEmail);
			chatRoom.setProjectId(projectId);
			chatRoom = chatRoomRepository.save(chatRoom);
		}

		// Redis 캐시 업데이트 (양쪽 사용자 모두)
		String summaryForEmail = createRoomSummaryJsonExtended(chatRoom, email, receiverEmail, null);
		String summaryForReceiver = createRoomSummaryJsonExtended(chatRoom, receiverEmail, email, null);
		String redisKeyForEmail = String.format(CHAT_ROOMS_KEY_TEMPLATE, email);
		String redisKeyForReceiver = String.format(CHAT_ROOMS_KEY_TEMPLATE, receiverEmail);

		redisTemplate.opsForHash().put(redisKeyForEmail, roomId, summaryForEmail);
		redisTemplate.expire(redisKeyForEmail, 60, TimeUnit.MINUTES);
		redisTemplate.opsForHash().put(redisKeyForReceiver, roomId, summaryForReceiver);
		redisTemplate.expire(redisKeyForReceiver, 60, TimeUnit.MINUTES);
	}

	/**
	 * 확장된 채팅방 요약 JSON 생성 (본인, 상대방, 마지막 메시지, projectId 포함)
	 */
	private String createRoomSummaryJsonExtended(ChatRoom room, String selfEmail, String otherEmail, ChatMessage lastMessage) {
		String lastMessageContent = "";
		String timestamp = null;
		if (lastMessage != null) {
			lastMessageContent = lastMessage.getContent().replace("\"", "\\\"").replace("\n", "\\n");
			timestamp = lastMessage.getTimestamp().toString();
		}
		return "{\"roomId\":\"" + room.getRoomId() + "\"," +
			"\"otherEmail\":\"" + otherEmail + "\"," +
			"\"lastMessage\":\"" + lastMessageContent + "\"," +
			(timestamp != null ? "\"timestamp\":\"" + timestamp + "\"," : "") +
			"\"projectId\":" + room.getProjectId() + "}";
	}

	/**
	 * 채팅방 읽음 처리
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
			log.error("Unread count 파싱 실패: {}", e.getMessage());
			return 0;
		}
	}

	/**
	 * 매 30분마다 Redis에 있는 채팅 메시지를 DB로 flush합니다.
	 */
	@Scheduled(fixedRate = 60000 * 30)
	@Transactional
	public void flushChatMessagesToDB() {
		Set<Object> activeRoomIds = redisTemplate.opsForSet().members(ACTIVE_ROOMS_KEY);
		if (activeRoomIds == null || activeRoomIds.isEmpty()) {
			log.info("활성 채팅방 없음.");
			return;
		}
		for (Object roomIdObj : activeRoomIds) {
			String roomId = (String) roomIdObj;
			String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, roomId);
			String lockKey = "lock:" + redisKey;
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCK", LOCK_EXPIRATION, TimeUnit.SECONDS);
			if (Boolean.TRUE.equals(acquired)) {
				try {
					List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
					if (cachedMessages != null && !cachedMessages.isEmpty()) {
						int originalSize = cachedMessages.size();
						List<ChatMessage> messages = objectMapper.convertValue(cachedMessages, new TypeReference<List<ChatMessage>>() {});
						List<ChatMessage> newMessages = messages.stream().filter(msg -> msg.getId() == null).collect(Collectors.toList());
						if (!newMessages.isEmpty()) {
							chatMessageRepository.saveAll(newMessages);
							log.info("채팅방 {}의 {}개 메시지 DB 저장", roomId, newMessages.size());
						} else {
							log.info("채팅방 {}에 저장할 신규 메시지 없음", roomId);
						}
						redisTemplate.opsForList().trim(redisKey, originalSize, -1);
					}
				} finally {
					redisTemplate.delete(lockKey);
				}
			} else {
				log.debug("채팅방 {} flush 작업 중", roomId);
			}
		}
	}
}

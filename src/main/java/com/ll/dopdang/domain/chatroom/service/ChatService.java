package com.ll.dopdang.domain.chatroom.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
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
	private final ExpertRepository expertRepository;

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
		chatMessage.setTimestamp(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

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

		// 메시지 객체에 채팅방 정보 설정
		chatMessage.setRoomId(chatRoom.getRoomId());
		chatMessage.setChatRoom(chatRoom);

		// 파트너 active 상태 확인:
		// sender가 member1이면 partner는 member2, 아니면 partner는 member1
		if (chatMessage.getSender().equals(chatRoom.getMember1())) {
			if (!chatRoom.isMemberActive2()) {
				// 프론트엔드에 /queue/partner-left 경로로 알림 전송
				messagingTemplate.convertAndSendToUser(
					chatMessage.getSender(), "/queue/partner-left", "상대방이 채팅방을 나갔습니다."
				);
				throw new ServiceException(ErrorCode.CHATTING_CLOSE_OTHER);
			}
		} else if (chatMessage.getSender().equals(chatRoom.getMember2())) {
			if (!chatRoom.isMemberActive1()) {
				messagingTemplate.convertAndSendToUser(
					chatMessage.getSender(), "/queue/partner-left", "상대방이 채팅방을 나갔습니다."
				);
				throw new ServiceException(ErrorCode.CHATTING_CLOSE_OTHER);
			}
		} else {
			throw new ServiceException(ErrorCode.CHATTING_SENDER_EQUAL);
		}

		// 메시지를 DB에 즉시 저장
		chatMessageRepository.save(chatMessage);

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
		List<ChatMessage> dbMessages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);

		// Redis에 메시지가 있는 경우
		if (cachedMessages != null && !cachedMessages.isEmpty()) {
			log.debug("Redis에서 채팅 메시지를 가져옵니다. roomId: {}", roomId);
			try {
				List<ChatMessage> redisMessages = objectMapper.convertValue(cachedMessages,
					new TypeReference<List<ChatMessage>>() {});

				// Redis 메시지의 일관성 검증
				if (isRedisDataConsistent(redisMessages, dbMessages)) {
					log.debug("Redis 데이터가 일관성이 있습니다. Redis 메시지를 반환합니다. roomId: {}", roomId);
					return redisMessages;
				} else {
					log.debug("Redis 데이터가 일관성이 없습니다. DB 메시지를 사용합니다. roomId: {}", roomId);
				}
			} catch (Exception e) {
				log.error("Redis 메시지 변환 중 오류 발생: {}, roomId: {}", e.getMessage(), roomId);
				// 변환 오류 시 Redis 캐시 삭제
				redisTemplate.delete(redisKey);
			}
		}

		// DB에서 메시지를 가져와 Redis에 저장
		log.debug("DB에서 채팅 메시지를 가져와 Redis에 저장합니다. roomId: {}", roomId);
		if (!dbMessages.isEmpty()) {
			// 분산 락 획득 시도
			String lockKey = "lock:" + redisKey;
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCK", LOCK_EXPIRATION, TimeUnit.SECONDS);

			if (Boolean.TRUE.equals(acquired)) {
				try {
					// Redis 캐시 초기화
					redisTemplate.delete(redisKey);

					// DB 메시지를 Redis에 저장
					for (ChatMessage message : dbMessages) {
						redisTemplate.opsForList().rightPush(redisKey, message);
					}
					redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
					redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, roomId);

					if (!dbMessages.isEmpty()) {
						ChatMessage latestMessage = dbMessages.get(dbMessages.size() - 1);
						updateChatRoomTimestamp(roomId, latestMessage.getTimestamp());
					}
				} finally {
					// 분산 락 해제
					redisTemplate.delete(lockKey);
				}
			} else {
				log.debug("다른 프로세스가 이미 Redis 캐시를 업데이트 중입니다. DB 메시지를 직접 반환합니다. roomId: {}", roomId);
			}
		}
		return dbMessages;
	}

	/**
	 * Redis 데이터의 일관성 검증
	 * Redis 메시지와 DB 메시지를 비교하여 일관성이 있는지 확인
	 */
	private boolean isRedisDataConsistent(List<ChatMessage> redisMessages, List<ChatMessage> dbMessages) {
		if (dbMessages.isEmpty()) return true;
		if (redisMessages.isEmpty()) return false;

		// 메시지 수 비교
		if (redisMessages.size() < dbMessages.size()) {
			return false;
		}

		// 최신 메시지의 타임스탬프 비교
		LocalDateTime latestRedisTimestamp = redisMessages.stream()
			.map(ChatMessage::getTimestamp)
			.max(LocalDateTime::compareTo)
			.orElse(null);

		LocalDateTime latestDbTimestamp = dbMessages.stream()
			.map(ChatMessage::getTimestamp)
			.max(LocalDateTime::compareTo)
			.orElse(null);

		if (latestRedisTimestamp == null || latestDbTimestamp == null) return false;

		// Redis의 최신 메시지가 DB의 최신 메시지보다 이전이면 일관성이 없는 것으로 간주
		if (latestRedisTimestamp.isBefore(latestDbTimestamp)) {
			return false;
		}

		// DB에 있는 모든 메시지 ID가 Redis에도 있는지 확인
		Set<Long> redisMessageIds = redisMessages.stream()
			.map(ChatMessage::getId)
			.filter(id -> id != null)
			.collect(Collectors.toSet());

		for (ChatMessage dbMessage : dbMessages) {
			if (dbMessage.getId() != null && !redisMessageIds.contains(dbMessage.getId())) {
				return false;
			}
		}

		return true;
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
		List<ChatRoom> dbRooms = chatRoomRepository.findByMember(member)
			.stream()
			.filter(room -> {
				if (room.getMember1().equalsIgnoreCase(member)) {
					return room.isMemberActive1();
				} else if (room.getMember2().equalsIgnoreCase(member)) {
					return room.isMemberActive2();
				}
				return false;
			})
			.collect(Collectors.toList());
		Member currentMember = memberRepository.findByEmail(member)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
		List<ChatRoomResponse> dtoList = dbRooms.stream().map(room -> {
			String otherEmail = room.getMember1().equalsIgnoreCase(member) ? room.getMember2() : room.getMember1();
			Member otherUser = memberRepository.findByEmail(otherEmail).orElse(null);
			// DB에서 최신 메시지 조회 (최대 1건, 내림차순 정렬)
			List<ChatMessage> messages = chatMessageRepository.findTopMessagesByRoomIdOrderByTimestampDesc(room.getRoomId(), 1);
			ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);
			// 만약 DB에서 최신 메시지가 없으면, Redis에서 조회
			if (lastMessage == null) {
				String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, room.getRoomId());
				List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
				if (cachedMessages != null && !cachedMessages.isEmpty()) {
					List<ChatMessage> redisMessages = objectMapper.convertValue(cachedMessages,
						new TypeReference<List<ChatMessage>>() {});
					lastMessage = redisMessages.isEmpty() ? null : redisMessages.get(redisMessages.size() - 1);
				}
			}
			int unreadCount = (int) getUnreadCount(room.getRoomId(), member);
			return ChatRoomResponse.from(room, currentMember, otherUser, lastMessage, unreadCount);
		}).collect(Collectors.toList());
		dtoList.sort((a, b) -> {
			LocalDateTime timeA = a.getLastMessageTime();
			LocalDateTime timeB = b.getLastMessageTime();
			if (timeA == null && timeB == null) return 0;
			if (timeA == null) return 1;
			if (timeB == null) return -1;
			return timeB.compareTo(timeA);
		});
		return dtoList;
	}

	/**
	 * 채팅방 나가기 로직: 주어진 채팅방에서 현재 사용자가 나간 것으로 표시하기 위해,
	 * 사용자가 멤버1이면 member1Active를, 멤버2이면 member2Active를 false로 설정합니다.
	 */
	@Transactional
	public void leaveChatRoomUser(String roomId, String userEmail) {
		// 채팅방 조회
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

		// 현재 사용자가 어느 멤버인지 확인 후 active 상태를 false로 설정
		if (chatRoom.getMember1().equalsIgnoreCase(userEmail)) {
			chatRoom.setMemberActive1(false);
		} else if (chatRoom.getMember2().equalsIgnoreCase(userEmail)) {
			chatRoom.setMemberActive2(false);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 채팅방의 멤버가 아닙니다.");
		}
		chatRoomRepository.save(chatRoom);

		// Redis 캐시 상에도 채팅방 요약 정보 갱신 또는 제거 필요할 수 있음(옵션)
		String userRedisKey = String.format(CHAT_ROOMS_KEY_TEMPLATE, userEmail);
		redisTemplate.opsForHash().delete(userRedisKey, roomId);

		log.info("사용자 {}가 채팅방 {}에서 나갔습니다.", userEmail, roomId);
	}

	/**
	 * 채팅방 캐시 업데이트 (각 사용자 Redis 해시 갱신)
	 */
	private void updateChatRoomCache(String roomId, ChatMessage chatMessage) {
		if (chatMessage == null) return;
		ChatRoom room = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new IllegalStateException("채팅방을 찾을 수 없습니다."));
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
			String content = chatMessage.getContent()
				.replace("\"", "\\\"")
				.replace("\n", "\\n");
			return "{\"roomId\":\"" + roomId + "\"," +
				"\"lastMessage\":\"" + content + "\"," +
				"\"timestamp\":\"" + chatMessage.getTimestamp() + "\"}";
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
	}

	public void createExpertChatroom(Long expertId, Long projectId) {
		ProjectDetailResponse projectDetail = projectService.getProjectById(projectId);
		String receiverEmail = projectDetail.getEmails().trim().toLowerCase();
		Expert expert = expertRepository.findById(expertId).orElseThrow();
		String email = expert.getMember().getEmail();

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
	}

	/**
	 * 채팅방 읽음 처리
	 */
	@Transactional
	public void markChatRoomAsRead(String roomId, String username) {
		ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
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
	 * 매 1분마다 Redis에 있는 채팅 메시지를 DB로 flush합니다.
	 */
	@Scheduled(fixedRate = 60000 * 1)
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
					// 현재 Redis 리스트 전체 데이터를 가져옵니다.
					List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
					if (cachedMessages != null && !cachedMessages.isEmpty()) {
						try {
							List<ChatMessage> messages = objectMapper.convertValue(cachedMessages,
								new TypeReference<List<ChatMessage>>() {});

							// 신규 메시지 : DB에 저장되지 않은 메시지 (id가 null인 경우)
							List<ChatMessage> newMessages = messages.stream()
								.filter(msg -> msg.getId() == null)
								.collect(Collectors.toList());

							int newMessagesCount = newMessages.size();
							if (newMessagesCount > 0) {
								chatMessageRepository.saveAll(newMessages);
								log.info("채팅방 {}의 {}개 신규 메시지 DB 저장", roomId, newMessagesCount);
							} else {
								log.debug("채팅방 {}에 저장할 신규 메시지 없음", roomId);
							}

							// 증분 업데이트: Redis 캐시를 완전히 삭제하지 않고 DB에 있는 메시지만 확인
							List<ChatMessage> messagesFromDB = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);

							// Redis 메시지와 DB 메시지의 일관성 검증
							if (!isRedisDataConsistent(messages, messagesFromDB)) {
								log.info("채팅방 {} Redis 데이터 일관성 검증 실패, 캐시 재구성", roomId);

								// 일관성이 없는 경우에만 Redis 캐시 재구성
								redisTemplate.delete(redisKey);
								for (ChatMessage message : messagesFromDB) {
									redisTemplate.opsForList().rightPush(redisKey, message);
								}
								redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
							} else {
								log.debug("채팅방 {} Redis 데이터 일관성 검증 성공", roomId);
								// 캐시 만료 시간만 갱신
								redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
							}
						} catch (Exception e) {
							log.error("채팅방 {} Redis 메시지 변환 중 오류 발생: {}", roomId, e.getMessage());

							// 변환 오류 시 Redis 캐시 재구성
							List<ChatMessage> messagesFromDB = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
							redisTemplate.delete(redisKey);
							for (ChatMessage message : messagesFromDB) {
								redisTemplate.opsForList().rightPush(redisKey, message);
							}
							redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
							log.info("채팅방 {} Redis 캐시 재구성 완료", roomId);
						}
					}
				} finally {
					// 분산 락 해제
					redisTemplate.delete(lockKey);
				}
			} else {
				log.debug("채팅방 {} flush 작업 중", roomId);
			}
		}
	}

}

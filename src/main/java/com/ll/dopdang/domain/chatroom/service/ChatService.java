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
import com.ll.dopdang.domain.chatroom.dto.ChatRoomSummary;
import com.ll.dopdang.domain.chatroom.dto.NotificationPayload;
import com.ll.dopdang.domain.chatroom.entity.ChatMessage;
import com.ll.dopdang.domain.chatroom.entity.ChatRoom;
import com.ll.dopdang.domain.chatroom.repository.ChatMessageRepository;
import com.ll.dopdang.domain.chatroom.repository.ChatRoomRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.project.dto.ProjectDetailResponse;
import com.ll.dopdang.domain.project.service.ProjectService;

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
	// 메시지 캐시와 관련된 채팅방 추적용 키
	private static final String ACTIVE_ROOMS_KEY = "chat:active_rooms";
	// Redis에 저장된 데이터의 만료 시간 (30분)
	private static final long CACHE_EXPIRATION = 30;
	// 분산 락의 기본 만료 시간 (예: 10초)
	private static final long LOCK_EXPIRATION = 10;

	/**
	 * 채팅 메시지를 저장합니다.
	 * (메시지는 Redis 캐시에만 저장합니다.)
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

		// 채팅방 캐시 업데이트 (Hash 방식)
		updateChatRoomCache(chatRoom.getRoomId(), chatMessage);

		// 채팅방 타임스탬프 업데이트 (메시지 전송 시간으로)
		updateChatRoomTimestamp(chatRoom.getRoomId(), chatMessage.getTimestamp());

		// 실시간 브로드캐스트 (WebSocket 이용)
		messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getRoomId(), chatMessage);

		// --- 알림 및 미읽은 메시지 처리 ---
		// 수신자가 현재 활성 채팅방에 있는지 확인
		String activeRoomKey = "active_chat_room:" + chatMessage.getReceiver();
		Object activeRoomObj = redisTemplate.opsForValue().get(activeRoomKey);
		if (activeRoomObj == null || !activeRoomObj.toString().equals(chatRoom.getRoomId())) {
			// 수신자가 해당 채팅방에 있지 않으면 미읽은 메시지 처리 및 알림 전송
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
			long updatedUnread = currentUnread + 1;
			redisTemplate.opsForValue().set(unreadKey, updatedUnread);

			NotificationPayload notification = new NotificationPayload(
				chatRoom.getRoomId(),         // 채팅방 ID
				chatMessage.getSender(),        // 발신자
				chatMessage.getContent(),       // 메시지 내용(요약)
				(int) updatedUnread            // 업데이트된 읽지 않은 메시지 수
			);
			messagingTemplate.convertAndSend("/topic/notice/" + chatMessage.getReceiver(), notification);
		}
	}

	/**
	 * 채팅방 타임스탬프 정보를 업데이트합니다.
	 *
	 * @param roomId    채팅방 ID
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
	 * Redis 캐시에 데이터가 있으면 캐시에서 조회하고,
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
			List<ChatMessage> redisMessages = objectMapper.convertValue(cachedMessages,
				new TypeReference<List<ChatMessage>>() {});
			LocalDateTime latestRedisMessageTime = redisMessages.isEmpty() ? null :
				redisMessages.get(redisMessages.size() - 1).getTimestamp();
			if (latestDbMessageTime == null || (latestRedisMessageTime != null && latestRedisMessageTime.isAfter(latestDbMessageTime))) {
				return redisMessages;
			}
			if (latestRedisMessageTime != null && !latestDbMessageTime.isEqual(latestRedisMessageTime)) {
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
				redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, roomId);
				ChatMessage latestMessage = messages.get(messages.size() - 1);
				updateChatRoomTimestamp(roomId, latestMessage.getTimestamp());
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
		Map<String, Member> memberCache = new HashMap<>();
		List<ChatRoomDetailResponse> responseList = new ArrayList<>();
		for (ChatMessage msg : messages) {
			// 캐시나 DB에서 발신자 Member 조회
			Member senderMember = memberCache.computeIfAbsent(msg.getSender(), email ->
				memberRepository.findByEmail(email).orElse(null)
			);
			ChatRoomDetailResponse detailDto = ChatRoomDetailResponse.from(msg, senderMember, true);
			responseList.add(detailDto);
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
	public List<ChatRoomResponse> getChatRoomsForUser(String member, Long currentUserId) {
		String redisKey = String.format(CHAT_ROOMS_KEY_TEMPLATE, member);
		Map<Object, Object> roomSummaries = redisTemplate.opsForHash().entries(redisKey);
		List<ChatRoomResponse> dtoList = new ArrayList<>();

		if (!roomSummaries.isEmpty()) {
			log.debug("Redis에서 채팅방 목록을 가져옵니다. member: {}", member);
			for (Map.Entry<Object, Object> entry : roomSummaries.entrySet()) {
				try {
					// Redis에 저장한 JSON을 ChatRoomSummary 객체로 변환
					ChatRoomSummary summary = objectMapper.readValue((String) entry.getValue(), ChatRoomSummary.class);
					int unreadCount = (int) getUnreadCount(summary.getRoomId(), member);

					// ChatRoomResponse 객체 생성 및 값 설정
					ChatRoomResponse response = new ChatRoomResponse();
					response.setRoomId(summary.getRoomId());
					response.setLastMessage(summary.getLastMessage());

					// timestamp 값이 null 또는 빈 문자열인 경우는 null로 처리
					String ts = summary.getTimestamp();
					if (ts != null && !ts.trim().isEmpty()) {
						response.setLastMessageTime(LocalDateTime.parse(ts));
					} else {
						response.setLastMessageTime(null);
					}
					response.setUnreadCount(unreadCount);
					response.setProjectId(summary.getProjectId());
					// 필요한 경우 sender, receiver, otherUserName 등 추가 설정

					dtoList.add(response);
				} catch (Exception e) {
					log.error("채팅방 요약 정보를 파싱하는 중 오류가 발생했습니다: {}", e.getMessage());
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
			log.debug("Redis 캐시가 없으므로 DB에서 채팅방 목록을 조회합니다. member: {}", member);
			List<ChatRoom> dbRooms = chatRoomRepository.findByMember(member);
			for (ChatRoom room : dbRooms) {
				// 상대방 이메일 결정
				String otherEmail = room.getMember1().equalsIgnoreCase(member) ? room.getMember2() : room.getMember1();
				List<ChatMessage> messages = chatMessageRepository.findTopMessagesByRoomIdOrderByTimestampDesc(room.getRoomId(), 1);
				ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(0);
				int unreadCount = (int) getUnreadCount(room.getRoomId(), member);

				Member currentMember = memberRepository.findByEmail(member)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
				Member otherUser = memberRepository.findByEmail(otherEmail).orElse(null);
				ChatRoomResponse response = ChatRoomResponse.from(room, currentMember, otherUser, lastMessage, unreadCount);
				dtoList.add(response);

				// Redis에 저장할 요약 JSON 갱신
				String summaryJson = createRoomSummaryJsonExtended(room, member, otherEmail, lastMessage);
				redisTemplate.opsForHash().put(redisKey, room.getRoomId(), summaryJson);
			}
			redisTemplate.expire(redisKey, CACHE_EXPIRATION, TimeUnit.MINUTES);
			// 최신 메시지 시간 기준 내림차순 정렬
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
	 * 채팅방 요약 정보 JSON 문자열 생성
	 * chatMessage가 null이면 기본 형식(채팅방 아이디만 포함)으로 생성합니다.
	 *
	 * @param roomId      채팅방 식별자
	 * @param chatMessage 최신 메시지 (없으면 null)
	 * @param projectId   채팅방에 연결된 프로젝트의 ID
	 * @return 채팅방 요약 JSON 문자열
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
	 * flush 작업 중 새 메시지가 들어오는 경우를 방지하기 위해 분산 락을 적용합니다.
	 */
	@Scheduled(fixedRate = 60000 * 30) // 매 30분마다 실행
	@Transactional
	public void flushChatMessagesToDB() {
		// ACTIVE_ROOMS_KEY에 등록된 채팅방 ID 집합을 조회
		Set<Object> activeRoomIds = redisTemplate.opsForSet().members(ACTIVE_ROOMS_KEY);
		if (activeRoomIds == null || activeRoomIds.isEmpty()) {
			log.info("활성 채팅방이 없습니다.");
			return;
		}
		for (Object roomIdObj : activeRoomIds) {
			String roomId = (String) roomIdObj;
			String redisKey = String.format(CHAT_MESSAGES_KEY_TEMPLATE, roomId);
			// 분산 락 키 생성 (예: lock:chat:messages:{roomId})
			String lockKey = "lock:" + redisKey;
			// 락 획득 시도 (최대 LOCK_EXPIRATION 초 동안 락 유지)
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCK", LOCK_EXPIRATION, TimeUnit.SECONDS);
			if (Boolean.TRUE.equals(acquired)) {
				try {
					List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
					if (cachedMessages != null && !cachedMessages.isEmpty()) {
						int originalSize = cachedMessages.size(); // flush 대상 메시지 개수 기록
						List<ChatMessage> messages = objectMapper.convertValue(
							cachedMessages, new TypeReference<List<ChatMessage>>() {});
						// 아직 DB에 저장되지 않은 메시지(즉, id가 null인 메시지)만 필터링
						List<ChatMessage> newMessages = messages.stream()
							.filter(msg -> msg.getId() == null)
							.collect(Collectors.toList());
						if (!newMessages.isEmpty()) {
							chatMessageRepository.saveAll(newMessages);
							log.info("채팅방 {}의 {}개의 새로운 메시지를 DB에 저장했습니다.", roomId, newMessages.size());
						} else {
							log.info("채팅방 {}에 저장할 새로운 메시지가 없습니다.", roomId);
						}
						// flush 당시 읽어온 메시지 개수만큼만 Redis 리스트에서 제거하여,
						// flush 이후에 새로 추가된 메시지는 그대로 유지
						redisTemplate.opsForList().trim(redisKey, originalSize, -1);
					}
				} finally {
					// 락 해제
					redisTemplate.delete(lockKey);
				}
			} else {
				log.debug("채팅방 {}에 대해 이미 flush 작업이 진행 중입니다.", roomId);
			}
		}
	}

	/**
	 * 채팅방 생성 로직
	 *
	 * @param email     현재 로그인한 사용자의 이메일
	 * @param projectId project 작성자 정보를 찾기 위한 id
	 */
	@Transactional
	public void createChatroom(String email, Long projectId) {
		// 1. 프로젝트 정보를 통해 상대방 이메일을 가져옵니다.
		ProjectDetailResponse projectDetail = projectService.getProjectById(projectId);
		String receiverEmail = projectDetail.getEmails().trim().toLowerCase();
		email = email.trim().toLowerCase();

		// 2. 채팅방 ID 결정 (알파벳 순 정렬 등)
		String roomId = getRoomId(email, receiverEmail);

		// 3. 채팅방 존재 여부 확인 및 저장 (없으면 새로 생성)
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

		// 4. Redis에 채팅방 요약 정보 업데이트
		//  - 각 사용자의 Redis 캐시에 채팅방 정보를 저장합니다.
		String summaryForEmail = createRoomSummaryJsonExtended(chatRoom, email, receiverEmail, null);
		String summaryForReceiver = createRoomSummaryJsonExtended(chatRoom, receiverEmail, email, null);
		String redisKeyForEmail = String.format(CHAT_ROOMS_KEY_TEMPLATE, email);
		String redisKeyForReceiver = String.format(CHAT_ROOMS_KEY_TEMPLATE, receiverEmail);

		redisTemplate.opsForHash().put(redisKeyForEmail, roomId, summaryForEmail);
		redisTemplate.expire(redisKeyForEmail, 60, TimeUnit.MINUTES);
		redisTemplate.opsForHash().put(redisKeyForReceiver, roomId, summaryForReceiver);
		redisTemplate.expire(redisKeyForReceiver, 60, TimeUnit.MINUTES);

		// ※ 삭제 로직이 없으므로, 채팅방 추가 시 Redis 동기화만 처리합니다.
	}
	/**
	 * 확장된 채팅방 요약 JSON 문자열 생성
	 * - 본인(selfEmail)과 상대방(otherEmail), 마지막 메시지(lastMessage) 등의 정보를 포함합니다.
	 */
	private String createRoomSummaryJsonExtended(ChatRoom room, String selfEmail, String otherEmail, ChatMessage lastMessage) {
		String lastMessageContent = "";
		String timestamp = null; // 기본값 null 지정
		if (lastMessage != null) {
			lastMessageContent = lastMessage.getContent().replace("\"", "\\\"").replace("\n", "\\n");
			timestamp = lastMessage.getTimestamp().toString();
		}
		// timestamp가 null일 경우 JSON에서도 null로 표기하거나 아예 필드를 생략할 수 있습니다.
		return "{\"roomId\":\"" + room.getRoomId() + "\"," +
			"\"otherEmail\":\"" + otherEmail + "\"," +
			"\"lastMessage\":\"" + lastMessageContent + "\"," +
			(timestamp != null ? "\"timestamp\":\"" + timestamp + "\"," : "") +
			"\"projectId\":" + room.getProjectId() + "}";
	}
}

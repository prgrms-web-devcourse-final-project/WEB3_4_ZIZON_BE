package com.ll.dopdang.domain.chatroom.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomSummary {
	private String roomId;
	private String otherEmail;
	private String lastMessage;
	private String timestamp;  // ISO 문자열 형식
	private Long projectId;

	// 기본 생성자, getter, setter 생략 (롬복 @Data 또는 직접 구현 가능)

	public LocalDateTime getTimestampAsLocalDateTime() {
		if (timestamp == null || timestamp.isEmpty()) {
			return LocalDateTime.MIN;
		}
		try {
			return LocalDateTime.parse(timestamp);
		} catch (DateTimeParseException e) {
			return LocalDateTime.MIN;
		}
	}

}

package com.ll.dopdang.global.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * API 에러 응답을 위한 DTO 클래스입니다.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

	private final int status;
	private final String message;
	private final LocalDateTime timestamp;

	@Builder.Default
	private List<FieldError> errors = new ArrayList<>();

	/**
	 * ErrorCode로부터 ErrorResponse 객체를 생성합니다.
	 *
	 * @param errorCode 에러 코드
	 * @return ErrorResponse 객체
	 */
	public static ErrorResponse of(ErrorCode errorCode) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus().value())
			.message(errorCode.getMessage())
			.timestamp(LocalDateTime.now())
			.build();
	}

	/**
	 * ErrorCode와 커스텀 메시지로부터 ErrorResponse 객체를 생성합니다.
	 *
	 * @param errorCode 에러 코드
	 * @param message 커스텀 메시지
	 * @return ErrorResponse 객체
	 */
	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return ErrorResponse.builder()
			.status(errorCode.getStatus().value())
			.message(message)
			.timestamp(LocalDateTime.now())
			.build();
	}

	/**
	 * 필드 에러 정보를 담는 내부 클래스
	 */
	@Getter
	@Builder
	public static class FieldError {
		private final String field;
		private final String value;
		private final String reason;
	}
}

package com.ll.dopdang.global.exception;

import lombok.Getter;

/**
 * 서비스 계층에서 발생하는 비즈니스 로직 관련 예외를 처리하기 위한 커스텀 예외 클래스입니다.
 */
@Getter
public class ServiceException extends RuntimeException {

	private final ErrorCode errorCode;

	/**
	 * 기본 생성자
	 *
	 * @param errorCode 에러 코드
	 */
	public ServiceException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	/**
	 * 상세 메시지가 있는 생성자
	 *
	 * @param errorCode 에러 코드
	 * @param message 상세 메시지
	 */
	public ServiceException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}

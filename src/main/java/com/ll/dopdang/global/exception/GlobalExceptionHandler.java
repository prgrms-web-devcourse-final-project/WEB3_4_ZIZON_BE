package com.ll.dopdang.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 처리
public class GlobalExceptionHandler {

	// 커스텀 예외 처리
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex) {
		ErrorCode errorCode = ex.getErrorCode();

		// 상황에 따라 warn 또는 error
		if (errorCode.getStatus().is5xxServerError()) {
			log.debug("❗[ServiceException] {} - {}", errorCode.name(), ex.getMessage(), ex);
		} else {
			log.debug("⚠️ [ServiceException] {} - {}", errorCode.name(), ex.getMessage());
		}

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ErrorResponse.of(errorCode));
	}

	// 처리되지 않은 예외 (예: NullPointerException 등)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		log.debug("‼️ [Unhandled Exception] {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

		return ResponseEntity
			.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
			.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}

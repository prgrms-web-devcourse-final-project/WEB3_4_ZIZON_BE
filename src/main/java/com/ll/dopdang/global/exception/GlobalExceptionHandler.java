package com.ll.dopdang.global.exception;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션에서 발생하는 모든 예외를 처리하는 전역 예외처리기입니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * ServiceException 처리를 위한 핸들러
	 *
	 * @param exception ServiceException
	 * @return 에러 응답
	 */
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ErrorResponse> handleServiceException(ServiceException exception) {
		log.error("ServiceException 발생: {}", exception.getMessage());

		ErrorCode errorCode = exception.getErrorCode();
		ErrorResponse response = ErrorResponse.builder()
			.status(errorCode.getStatus().value())
			.message(exception.getMessage())
			.timestamp(LocalDateTime.now())
			.build();

		return new ResponseEntity<>(response, errorCode.getStatus());
	}

	/**
	 * HTTP 메시지 변환 오류 처리
	 * 날짜 형식 오류에 대한 명확한 메시지 제공
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception) {
		log.error("메시지 변환 오류: {}", exception.getMessage());

		String message = "요청 본문을 처리할 수 없습니다.";

		// 날짜 형식 오류 처리
		if (exception.getCause() instanceof InvalidFormatException ife) {
			if (ife.getTargetType() != null
				&& (ife.getTargetType().equals(LocalDateTime.class)
				|| ife.getTargetType().equals(LocalDate.class))) {
				message = "날짜 형식이 올바르지 않습니다. 'yyyy-MM-dd' 또는 'yyyy-MM-dd'T'HH:mm:ss' 형식을 사용해주세요.";
			}
		} else if (exception.getCause() instanceof IllegalArgumentException
			&& exception.getCause().getMessage() != null
			&& exception.getCause().getMessage().contains("날짜 형식이 올바르지 않습니다")) {
			message = exception.getCause().getMessage();
		}

		ErrorResponse response = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message(message)
			.timestamp(LocalDateTime.now())
			.build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * 입력값 검증 실패 예외 처리
	 */
	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity<ErrorResponse> handleValidationException(Exception exception) {
		log.error("입력값 검증 실패: {}", exception.getMessage());

		ErrorResponse response = ErrorResponse.builder()
			.status(HttpStatus.BAD_REQUEST.value())
			.message("입력값이 올바르지 않습니다.")
			.timestamp(LocalDateTime.now())
			.build();

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * 접근 권한 예외 처리를 위한 핸들러
	 *
	 * @param exception AccessDeniedException
	 * @return 에러 응답
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
		log.error("접근 권한 예외 발생: {}", exception.getMessage());

		ErrorResponse response = ErrorResponse.builder()
			.status(HttpStatus.FORBIDDEN.value())
			.message("접근 권한이 없습니다.")
			.timestamp(LocalDateTime.now())
			.build();

		return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
	}

	/**
	 * 기타 예외 처리를 위한 핸들러
	 *
	 * @param exception Exception
	 * @return 에러 응답
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception) {
		log.error("예외 발생: {}", exception.getMessage(), exception);

		ErrorResponse response = ErrorResponse.builder()
			.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
			.message("서버 내부 오류가 발생했습니다.")
			.timestamp(LocalDateTime.now())
			.build();

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}

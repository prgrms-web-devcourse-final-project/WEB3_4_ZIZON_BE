package com.ll.dopdang.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 서비스 예외에 사용되는 에러 코드를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통 에러
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),

	// 결제 관련 에러
	PAYMENT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),
	PAYMENT_CONFIRMATION_FAILED(HttpStatus.BAD_REQUEST, "결제 승인에 실패했습니다."),
	PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보를 찾을 수 없습니다."),
	CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "계약 정보를 찾을 수 없습니다."),
	PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 결제가 완료된 건입니다."),
	PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
	PAYMENT_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 결제입니다."),
	PAYMENT_CANCELLATION_FAILED(HttpStatus.BAD_REQUEST, "결제 취소에 실패했습니다."),
	INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 취소 금액입니다."),
	PAYMENT_METADATA_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 메타데이터 업데이트 중 오류가 발생했습니다."),
	PAYMENT_CANCEL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토스페이먼츠 결제 취소 API 호출 실패"),

	// 계약 관련 에러
	CONTRACT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "계약 처리 중 오류가 발생했습니다."),
	OFFER_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 오퍼입니다."),

	// 회원 관련 에러
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}

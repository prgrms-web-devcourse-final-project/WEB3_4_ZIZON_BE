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

	// 계약 관련 에러
	CONTRACT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "계약 처리 중 오류가 발생했습니다."),
	OFFER_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 오퍼입니다."),

	// 회원 관련 에러
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
	NOT_A_SOCIAL_USER(HttpStatus.BAD_REQUEST, "해당 유저는 소셜 유저가 아닙니다."),
	PHONE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "해당 유저는 이미 전화번호를 인증하였습니다."),
	PHONE_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "전화번호 인증에 실패하였습니다."),
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 유저입니다.");

	private final HttpStatus status;
	private final String message;
}

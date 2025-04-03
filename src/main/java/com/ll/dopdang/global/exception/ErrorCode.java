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
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
	ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 데이터를 찾을 수 없습니다."),

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

	// 문자 관련 에러
	MESSAGE_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "문자 발송에 실패하였습니다."),

	// 토큰 관련 에러
	INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),

	// 회원 관련 에러
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
	NOT_A_SOCIAL_USER(HttpStatus.BAD_REQUEST, "해당 유저는 소셜 유저가 아닙니다."),
	PHONE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "해당 유저는 이미 전화번호를 인증하였습니다."),
	PHONE_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "전화번호 인증에 실패하였습니다."),
	PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "현재 사용중인 비밀번호와 일치합니다."),
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 유저입니다."),
    
  // S3 Presigned URL 생성 관련
	PRESIGNED_URL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 중 알 수 없는 오류가 발생했습니다."),
	INVALID_S3_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청으로 인해 S3 Presigned URL 생성에 실패했습니다."),
	S3_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S3 접근 권한이 없습니다."),
	S3_BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "지정된 S3 버킷을 찾을 수 없습니다."),
	AWS_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWS 클라이언트 설정 오류 또는 자격 증명 오류가 발생했습니다.");


	private final HttpStatus status;
	private final String message;
}

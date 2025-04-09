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

	// 문자 관련 에러
	MESSAGE_SENDING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "문자 발송에 실패하였습니다."),

	// 토큰 관련 에러
	INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다."),

	// 회원 관련 에러
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
	NOT_A_SOCIAL_USER(HttpStatus.BAD_REQUEST, "해당 유저는 소셜 유저가 아닙니다."),
	NOT_A_EXPERT_USER(HttpStatus.FORBIDDEN, "해당 유저는 전문가가 아닙니다."),
	PHONE_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "해당 유저는 이미 전화번호를 인증하였습니다."),
	PHONE_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "전화번호 인증에 실패하였습니다."),
	PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "현재 사용중인 비밀번호와 일치합니다."),
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 유저입니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

	// 카테고리 관련 에러
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),

	// 전문가 관련 에러
	EXPERT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 전문가입니다."),
	INVALID_EXPERT_ASSIGNMENT(HttpStatus.BAD_REQUEST, "지정된 전문가 정보를 확인할 수 없습니다."),

	// 프로젝트 관련 에러
	PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."),
	INVALID_PROJECT_STATUS(HttpStatus.BAD_REQUEST, "프로젝트 상태 값이 올바르지 않습니다."),

	//계약서 관련에러
	UNAUTHORIZED_CONTRACT_CREATION(HttpStatus.FORBIDDEN, "해당 프로젝트의 클라이언트만 계약을 생성할 수 있습니다."),
	CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 계약을 찾을 수 없습니다."),
	CONTRACT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 계약에 접근할 수 없습니다."),

	// 제안서 관련 에러
	OFFER_NOT_FOUND(HttpStatus.NOT_FOUND, "제안서를 찾을 수 없습니다."),
	INVALID_OFFER_PROJECT(HttpStatus.BAD_REQUEST, "제안서에 대한 프로젝트 정보가 올바르지 않습니다."),

	// 문의글 관련 에러
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "문의글을 찾을 수 없습니다."),
	INVALID_POST_AUTHOR(HttpStatus.UNAUTHORIZED, "문의글 작성자가 아닙니다."),
	INVALID_COMMENT_AUTHOR(HttpStatus.UNAUTHORIZED, "댓글은 관리자만 작성할 수 있습니다."),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
	DISMATCH_COMMENT_AND_POST(HttpStatus.BAD_REQUEST, "댓글과 문의글의 연관관계가 일치하지 않습니다."),

	// 상품 관련 에러
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
	INVALID_PRODUCT_CONTENT(HttpStatus.BAD_REQUEST, "디지털 상품의 내용은 필수입니다."),
	INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "상품의 가격은 0원 이상이어야 합니다."),
	INVALID_PRODUCT_STOCK(HttpStatus.BAD_REQUEST, "상품의 재고는 -1 이상이어야 합니다."),

	// S3 Presigned URL 생성 관련
	PRESIGNED_URL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 중 알 수 없는 오류가 발생했습니다."),
	INVALID_S3_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청으로 인해 S3 Presigned URL 생성에 실패했습니다."),
	S3_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S3 접근 권한이 없습니다."),
	S3_BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "지정된 S3 버킷을 찾을 수 없습니다."),
	S3_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "지정된 S3 버킷을 찾을 수 없습니다."),
	AWS_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWS 클라이언트 설정 오류 또는 자격 증명 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String message;
}

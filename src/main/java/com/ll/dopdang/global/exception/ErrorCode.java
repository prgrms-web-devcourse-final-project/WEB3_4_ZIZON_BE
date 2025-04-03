package com.ll.dopdang.global.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {


	// S3 Presigned URL 생성 관련
	PRESIGNED_URL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 중 알 수 없는 오류가 발생했습니다."),
	INVALID_S3_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청으로 인해 S3 Presigned URL 생성에 실패했습니다."),
	S3_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S3 접근 권한이 없습니다."),
	S3_BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "지정된 S3 버킷을 찾을 수 없습니다."),
	AWS_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWS 클라이언트 설정 오류 또는 자격 증명 오류가 발생했습니다."),


	// 공통 예외
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
	ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 데이터를 찾을 수 없습니다.");
	private final HttpStatus status;
	private final String message;
}

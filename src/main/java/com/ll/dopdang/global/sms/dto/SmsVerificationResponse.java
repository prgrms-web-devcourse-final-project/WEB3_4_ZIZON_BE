package com.ll.dopdang.global.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문자 인증 response
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerificationResponse {
	/**
	 * 성공 여부
	 */
	private boolean success;
	/**
	 * 출력 메세지
	 */
	private String message;
}

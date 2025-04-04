package com.ll.dopdang.global.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전화번호 인증 request
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerificationRequest {
	/**
	 * 전화번호
	 */
	@NotBlank
	@Pattern(regexp = "^(010\\d{8}|011\\d{7})$", message = "전화번호는 대시없이 입력하셔야 합니다.")
	private String phoneNumber;
	/**
	 * 인증 코드
	 */
	@NotBlank
	@Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자입니다.")
	private String verificationCode;
}

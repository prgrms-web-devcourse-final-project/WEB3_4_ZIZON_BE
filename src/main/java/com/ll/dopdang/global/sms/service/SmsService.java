package com.ll.dopdang.global.sms.service;

import com.ll.dopdang.global.sms.dto.SmsVerificationRequest;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;

/**
 * CoolSmsService의 인터페이스 클래스
 */
public interface SmsService {
	/**
	 * 인증번호를 생성하고 SMS로 전송합니다.
	 *
	 * @param phoneNumber 인증번호를 받을 전화번호
	 * @return 인증 요청 결과
	 */
	SmsVerificationResponse sendVerificationCode(String phoneNumber);

	/**
	 * 사용자가 입력한 인증번호를 검증합니다.
	 *
	 * @param request 전화번호와 인증번호가 포함된 요청
	 * @return 인증 결과
	 */
	boolean verifyCode(SmsVerificationRequest request);
}

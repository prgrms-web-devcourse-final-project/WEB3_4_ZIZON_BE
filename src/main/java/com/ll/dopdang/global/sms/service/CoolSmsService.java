package com.ll.dopdang.global.sms.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;

import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.sms.dto.SmsVerificationRequest;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CoolSMS의 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoolSmsService implements SmsService {
	@Value("${coolsms.api_key}")
	private String apiKey;
	@Value("${coolsms.api_secret}")
	private String apiSecret;
	@Value("${coolsms.from_number}")
	private String fromNumber;
	private final RedisRepository redisRepository;
	private static final int VERIFICATION_CODE_LENGTH = 6;
	private static final int VERIFICATION_CODE_EXPIRATION = 5 * 60; // 5분
	private DefaultMessageService messageService;
	private final Random random = new Random();

	/**
	 * 메세지 가져오기
	 * @return {@link DefaultMessageService}
	 */
	private DefaultMessageService getMessageService() {
		if (messageService == null) {
			messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
		}
		return messageService;
	}

	/**
	 * 인증 코드 보내기 메서드
	 * @param phoneNumber 인증번호를 받을 전화번호
	 * @return {@link SmsVerificationResponse}
	 */
	@Override
	public SmsVerificationResponse sendVerificationCode(String phoneNumber) {
		try {
			// 인증번호 생성
			String verificationCode = generateVerificationCode();

			// Redis에 인증번호 저장 (5분 유효)
			redisRepository.save("SMS:" + phoneNumber, verificationCode, VERIFICATION_CODE_EXPIRATION,
				TimeUnit.SECONDS);

			// SMS 메시지 내용
			String messageBody = "[Final Project] 인증번호: " + verificationCode + "\n인증번호는 5분간 유효합니다.";

			// CoolSMS를 통해 SMS 발송
			Message message = new Message();
			message.setFrom(fromNumber);
			message.setTo(phoneNumber);
			message.setText(messageBody);

			// 메시지 발송
			getMessageService().sendOne(new SingleMessageSendingRequest(message));

			log.info("SMS sent to: {}", phoneNumber);

			return SmsVerificationResponse.builder()
				.success(true)
				.message("인증번호가 발송되었습니다.")
				.build();

		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("SMS 발송 실패: {}", e.getMessage(), e);
			}

			throw new ServiceException(ErrorCode.MESSAGE_SENDING_ERROR);
		}
	}

	/**
	 * 인증 코드 검증 메서드
	 * @param request 전화번호와 인증번호가 포함된 요청
	 * @return {@link Boolean}
	 */
	@Override
	public boolean verifyCode(SmsVerificationRequest request) {
		String storedCode = (String)redisRepository.get("SMS:" + request.getPhoneNumber());

		if (storedCode == null) {
			return false; // 인증번호가 만료되었거나 존재하지 않음
		}

		boolean isValid = storedCode.equals(request.getVerificationCode());

		if (isValid) {
			// 인증 성공 시 Redis에서 인증번호 삭제
			redisRepository.remove("SMS:" + request.getPhoneNumber());
		}

		return isValid;
	}

	/**
	 * 인증 코드 생성 메서드
	 * @return {@link String}
	 */
	private String generateVerificationCode() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
			sb.append(random.nextInt(10));
		}

		return sb.toString();
	}
}

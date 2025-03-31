package com.ll.dopdang.domain.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.sms.dto.SmsVerificationRequest;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;
import com.ll.dopdang.global.sms.service.SmsService;

import lombok.RequiredArgsConstructor;

/**
 * MemberService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
	/**
	 * 유저 레포지터리
	 */
	private final MemberRepository memberRepository;
	/**
	 * 비밀번호 인코더
	 */
	private final PasswordEncoder passwordEncoder;
	/**
	 * redis 레포지터리
	 */
	private final RedisRepository redisRepository;
	/**
	 * 문자 발송 서비스
	 */
	private final SmsService smsService;
	/**
	 * ObjectMapper
	 */
	private final ObjectMapper objectMapper;

	/**
	 * 회원가입 메서드
	 * @param req 회원가입 dto
	 * @param code 인증 코드
	 */
	@Transactional
	public void signup(MemberSignupRequest req, String code) {
		memberRepository.findByEmail(req.getEmail()).ifPresent(m -> {
			throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
		});

		boolean isVerified = verify(req.getPhone(), code);
		if (!isVerified) {
			throw new RuntimeException("전화번호 인증에 실패하였습니다.");
		}

		Member member = Member.builder()
			.email(req.getEmail())
			.password(passwordEncoder.encode(req.getPassword()))
			.name(req.getName())
			.phone(req.getPhone())
			.profileImage("")
			.status(MemberStatus.ACTIVE.toString())
			.userRole(MemberRole.ROLE_CLIENT.toString())
			.memberId(req.getEmail())
			.build();
		memberRepository.save(member);
		redisRepository.remove("VERIFIED_PHONE:" + req.getPhone());
	}

	/**
	 * 인증 코드 검증 메서드
	 * @param phone 전화번호
	 * @param code 인증번호
	 * @return {@link Boolean}
	 */
	public boolean verify(String phone, String code) {
		SmsVerificationRequest req = new SmsVerificationRequest(phone, code);
		return smsService.verifyCode(req);
	}

	/**
	 * 인증번호 발송 메서드
	 * @param phone 전화번호
	 * @return {@link SmsVerificationResponse}
	 */
	public SmsVerificationResponse sendCode(String phone) {
		return smsService.sendVerificationCode(phone);
	}
}

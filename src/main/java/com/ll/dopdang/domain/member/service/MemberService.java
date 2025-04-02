package com.ll.dopdang.domain.member.service;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.dto.request.VerifyCodeRequest;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.sms.dto.SmsVerificationRequest;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;
import com.ll.dopdang.global.sms.service.SmsService;

import lombok.RequiredArgsConstructor;

/**
 * MemberService
 */
@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final RedisRepository redisRepository;
	private final SmsService smsService;

	/**
	 * 회원가입 메서드
	 * @param req 회원가입 dto
	 * @param code 인증 코드
	 */
	@Transactional(rollbackFor = Exception.class)
	public void signup(MemberSignupRequest req, String code) {
		memberRepository.findByEmail(req.getEmail()).ifPresent(m -> {
			throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
		});

		boolean isVerified = verify(req.getVerifyCodeRequest().getPhone(), code);
		if (!isVerified) {
			throw new IllegalArgumentException("전화번호 인증에 실패하였습니다.");
		}

		Member member = Member.builder()
			.email(req.getEmail())
			.password(passwordEncoder.encode(req.getPassword()))
			.name(req.getName())
			.phone(req.getVerifyCodeRequest().getPhone())
			.profileImage("")
			.status(MemberStatus.ACTIVE.toString())
			.userRole(MemberRole.CLIENT.toString())
			.memberId(req.getEmail())
			.build();
		memberRepository.save(member);
		redisRepository.remove("VERIFIED_PHONE:" + req.getVerifyCodeRequest().getPhone());
	}

	/**
	 * 소셜 로그인 유저의 전화번호 인증 메서드
	 * @param id 유저 고유 ID
	 * @param code 인증번호
	 * @param req 전화번호 인증 dto
	 * @param customUserDetails 인증된 사용자 정보
	 */
	@Transactional
	public void verifyPhone(Long id, String code, VerifyCodeRequest req, CustomUserDetails customUserDetails) {
		isValidMember(id, customUserDetails);
		Member member = findMember(id);

		if (member.getMemberId().equals(member.getEmail())) {
			throw new ServiceException(ErrorCode.NOT_A_SOCIAL_USER);
		}

		// 전화번호 수정 기능이 들어가면 사라질 로직
		if (MemberStatus.ACTIVE.toString().equals(member.getStatus())) {
			throw new ServiceException(ErrorCode.PHONE_ALREADY_VERIFIED);
		}

		boolean isVerified = verify(req.getPhone(), code);
		if (!isVerified) {
			throw new ServiceException(ErrorCode.PHONE_VERIFICATION_FAILED);
		}

		member.activateMember();
		member.activatePhone(req.getPhone());
		redisRepository.remove("VERIFIED_PHONE:" + req.getPhone());
	}

	/**
	 * Member 가져 오기
	 * @param id 유저 고유 ID
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link Member}
	 */
	@Transactional(readOnly = true)
	public Member getMember(Long id, CustomUserDetails customUserDetails) {
		isValidMember(id, customUserDetails);
		return findMember(id);
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

	/**
	 * 회원 검증 메서드
	 * @param id 유저 고유 ID
	 * @param customUserDetails 인증된 사용자 정보
	 */
	public void isValidMember(Long id, CustomUserDetails customUserDetails) {
		if (ObjectUtils.isEmpty(customUserDetails)) {
			throw new ServiceException(ErrorCode.MEMBER_NOT_FOUND);
		}
		if (!Objects.equals(customUserDetails.getMember().getId(), id)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}
	}

	/**
	 * 유저 ID를 사용한 유저 찾기 메서드
	 * @param id 유저 고유 ID
	 * @return {@link Member}
	 */
	@Transactional(readOnly = true)
	public Member findMember(Long id) {
		return memberRepository.findById(id).orElseThrow(
			() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));
	}
}

package com.ll.dopdang.domain.member.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.sms.dto.SmsVerificationRequest;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;
import com.ll.dopdang.global.sms.service.SmsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberUtilService {
	private final MemberRepository memberRepository;
	private final SmsService smsService;

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

	/**
	 * 유저가 Expert인지 검증하는 메서드
	 * @param member 회원
	 * @return {@link Expert} 전문가 정보
	 * @throws ServiceException 전문가가 아닌 경우 예외 발생
	 */
	@Transactional
	public Expert validateExpert(Member member) {
		if (!Objects.equals(member.getUserRole(), MemberRole.EXPERT.toString())) {
			throw new ServiceException(ErrorCode.NOT_A_EXPERT_USER);
		}

		return member.getExpert();
	}

	@Transactional
	public Expert checkExpertAuthorization(Member member, Product product) {
		if (!Objects.equals(member.getExpert(), product.getExpert())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}

		return member.getExpert();
	}

}

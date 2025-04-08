package com.ll.dopdang.domain.member.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.dto.request.UpdateProfileRequest;
import com.ll.dopdang.domain.member.dto.request.VerifyCodeRequest;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

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
	private final MemberUtilService memberUtilService;

	// Todo: MemberUtilService의 findMember() 사용하도록 코드 수정
	public Member getMemberById(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));
	}

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

		boolean isVerified = memberUtilService.verify(req.getVerifyCodeRequest().getPhone(), code);
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
		memberUtilService.isValidMember(id, customUserDetails);
		Member member = memberUtilService.findMember(id);

		if (member.getMemberId().equals(member.getEmail())) {
			throw new ServiceException(ErrorCode.NOT_A_SOCIAL_USER);
		}

		// 전화번호 수정 기능이 들어가면 사라질 로직
		if (MemberStatus.ACTIVE.toString().equals(member.getStatus())) {
			throw new ServiceException(ErrorCode.PHONE_ALREADY_VERIFIED);
		}

		boolean isVerified = memberUtilService.verify(req.getPhone(), code);
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
		memberUtilService.isValidMember(id, customUserDetails);
		return memberUtilService.findMember(id);
	}

	/**
	 * Member 수정 하기
	 * @param id 유저 고유 ID
	 * @param req 회원 정보 수정 dto
	 * @param customUserDetails 인증된 사용자 정보
	 */
	@Transactional
	public void updateMember(Long id, UpdateProfileRequest req, CustomUserDetails customUserDetails) {
		memberUtilService.isValidMember(id, customUserDetails);
		Member member = memberUtilService.findMember(id);

		Member updateMember = Member.builder()
			.id(member.getId())
			.email(member.getEmail())
			.password(member.getPassword())
			.name(!req.getName().isBlank() ? req.getName() : member.getName())
			.profileImage(!req.getProfileImage().isBlank() ? req.getProfileImage() : member.getProfileImage())
			.phone(member.getPhone())
			.status(member.getStatus())
			.userRole(member.getUserRole())
			.memberId(member.getMemberId())
			.uniqueKey(member.getUniqueKey())
			.createdAt(member.getCreatedAt())
			.updatedAt(LocalDateTime.now())
			.build();
		memberRepository.save(updateMember);
	}

	@Transactional
	public void deleteMember(Long id, CustomUserDetails customUserDetails) {
		memberUtilService.isValidMember(id, customUserDetails);
		Member member = memberUtilService.findMember(id);

		Member deleteMember = Member.builder()
			.id(member.getId())
			.email(member.getEmail())
			.password(member.getPassword())
			.name(member.getName())
			.profileImage(member.getProfileImage())
			.phone(member.getPhone())
			.status(MemberStatus.DEACTIVATED.toString())
			.memberId(member.getMemberId())
			.uniqueKey(member.getUniqueKey())
			.createdAt(member.getCreatedAt())
			.updatedAt(LocalDateTime.now())
			.build();
		memberRepository.save(deleteMember);
	}
}

package com.ll.dopdang.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.dto.request.VerifyCodeRequest;
import com.ll.dopdang.domain.member.dto.response.MemberInfoResponse;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * MemberController
 * <p>
 *     유저 컨트롤러
 * </p>
 *
 * @author sungyeong98
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class MemberController {
	/**
	 * 유저 서비스
	 */
	private final MemberService memberService;

	/**
	 * 회원 가입 메서드
	 * @param req 회원가입 dto
	 * @return {@link ResponseEntity}
	 */
	@PostMapping("/signup")
	public ResponseEntity<Object> signup(
		@Valid @RequestBody MemberSignupRequest req) {
		if (req.getVerifyCodeRequest().getCode() == null) {
			SmsVerificationResponse resp = memberService.sendCode(req.getVerifyCodeRequest().getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.signup(req, req.getVerifyCodeRequest().getCode());
		return ResponseEntity.ok("회원 가입 성공");
	}

	/**
	 * 소셜 유저의 전화번호 인증 메서드
	 * @param userId 유저 고유 ID
	 * @param request 전화번호 인증 dto
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@PostMapping("/{user_id}/phone-verification")
	public ResponseEntity<Object> verifyPhone(
		@PathVariable("user_id") Long userId,
		@Valid @RequestBody VerifyCodeRequest request,
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		if (request.getCode() == null) {
			SmsVerificationResponse resp = memberService.sendCode(request.getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.verifyPhone(userId, request.getCode(), request, customUserDetails);
		return ResponseEntity.ok("전화번호 인증이 완료되었습니다.");
	}

	@GetMapping("/me")
	public ResponseEntity<MemberInfoResponse> getCurrentUser(
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		if (customUserDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		MemberInfoResponse response = new MemberInfoResponse(
			customUserDetails.getMember().getId(),
			customUserDetails.getUsername(),
			customUserDetails.getMember().getName()
		);
		return ResponseEntity.ok(response);
	}
}

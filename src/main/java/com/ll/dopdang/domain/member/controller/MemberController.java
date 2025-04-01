package com.ll.dopdang.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.dto.request.VerifyCodeRequest;
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
	 * @param code 인증 코드
	 * @return {@link ResponseEntity}
	 */
	@PostMapping("/signup")
	public ResponseEntity<Object> signup(
		@Valid @RequestBody MemberSignupRequest req,
		@RequestParam String code) {
		if (code.equals("request")) {
			SmsVerificationResponse resp = memberService.sendCode(req.getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.signup(req, code);
		return ResponseEntity.ok("회원 가입 성공");
	}

	/**
	 * 소셜 유저의 전화번호 인증 메서드
	 * @param userId 유저 고유 ID
	 * @param code 인증번호
	 * @param request 전화번호 인증 dto
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@PostMapping("/{user_id}/phone-verification")
	public ResponseEntity<Object> verifyPhone(
		@PathVariable("user_id") Long userId,
		@RequestParam String code,
		@Valid @RequestBody VerifyCodeRequest request,
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		if (code.equals("request")) {
			SmsVerificationResponse resp = memberService.sendCode(request.getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.verifyPhone(userId, code, request, customUserDetails);
		return ResponseEntity.ok("전화번호 인증이 완료되었습니다.");
	}
}

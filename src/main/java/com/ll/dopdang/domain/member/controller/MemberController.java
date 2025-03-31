package com.ll.dopdang.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.service.MemberService;
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
}

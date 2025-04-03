package com.ll.dopdang.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.member.dto.request.MemberSignupRequest;
import com.ll.dopdang.domain.member.dto.request.UpdateProfileRequest;
import com.ll.dopdang.domain.member.dto.request.VerifyCodeRequest;
import com.ll.dopdang.domain.member.dto.response.ProfileResponse;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.member.service.MemberUtilService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "사용자 API", description = "일반 사용자 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class MemberController {
	private final MemberService memberService;
	private final MemberUtilService memberUtilService;

	/**
	 * 회원 가입 API
	 * @param req 회원가입 dto
	 * @return {@link ResponseEntity}
	 */
	@Operation(summary = "회원 가입", description = "일반 사용자의 회원가입을 위한 API 입니다.")
	@PostMapping("/signup")
	public ResponseEntity<Object> signup(
		@Valid @RequestBody MemberSignupRequest req) {
		if (req.getVerifyCodeRequest().getCode() == null) {
			SmsVerificationResponse resp = memberUtilService.sendCode(req.getVerifyCodeRequest().getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.signup(req, req.getVerifyCodeRequest().getCode());
		return ResponseEntity.ok("회원 가입 성공");
	}

	/**
	 * 소셜 유저의 전화번호 인증 API
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
			SmsVerificationResponse resp = memberUtilService.sendCode(request.getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.verifyPhone(userId, request.getCode(), request, customUserDetails);
		return ResponseEntity.ok("전화번호 인증이 완료되었습니다.");
	}

	/**
	 * 사용자 마이페이지 조회 API
	 * @param userId 유저 고유 ID
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@GetMapping("/{user_id}")
	public ResponseEntity<Object> getMember(
		@PathVariable("user_id") Long userId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		return ResponseEntity.ok(new ProfileResponse(memberService.getMember(userId, customUserDetails)));
	}

	/**
	 * 사용자 마이페이지 수정 API
	 * @param userId 유저 고유 Id
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@PatchMapping("/{user_id}")
	public ResponseEntity<Object> updateMember(
		@PathVariable("user_id") Long userId,
		@Valid @RequestBody UpdateProfileRequest req,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		memberService.updateMember(userId, req, customUserDetails);
		return ResponseEntity.ok("수정을 완료하였습니다.");
	}

	@DeleteMapping("/{user_id}")
	public ResponseEntity<Object> deleteMember(
		@PathVariable("user_id") Long userId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		memberService.deleteMember(userId, customUserDetails);
		return ResponseEntity.ok("회원 탈퇴가 정상적으로 처리되었습니다.");
	}
}

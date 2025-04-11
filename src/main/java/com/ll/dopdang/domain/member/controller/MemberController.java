package com.ll.dopdang.domain.member.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
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
import com.ll.dopdang.domain.member.dto.request.PasswordUpdateRequest;
import com.ll.dopdang.domain.member.dto.request.UpdateProfileRequest;
import com.ll.dopdang.domain.member.dto.request.VerifyCodeRequest;
import com.ll.dopdang.domain.member.dto.response.MemberInfoResponse;
import com.ll.dopdang.domain.member.dto.response.ProfileResponse;
import com.ll.dopdang.domain.member.dto.response.UpdateProfileResponse;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.member.service.MemberUtilService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.sms.dto.SmsVerificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
	@Operation(
		summary = "회원 가입",
		description = "휴대폰 인증 코드가 없으면 인증 요청을 먼저 수행하고, 있으면 회원 가입을 완료합니다."
	)
	@ApiResponse(responseCode = "200", description = "회원 가입 또는 인증 요청 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
	@PostMapping("/signup")
	public ResponseEntity<Object> signup(
		@Valid @RequestBody MemberSignupRequest req) {
		if (req.getVerifyCodeRequest().getCode() == null) {
			SmsVerificationResponse resp = memberUtilService.sendCode(req.getVerifyCodeRequest().getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.signup(req, req.getVerifyCodeRequest().getCode());
		return ResponseEntity.ok().body(Map.of("message", "회원 가입 성공"));
	}

	/**
	 * 소셜 유저의 전화번호 인증 API
	 * @param userId 유저 고유 ID
	 * @param request 전화번호 인증 dto
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@Operation(
		summary = "소셜 유저 전화번호 인증",
		description = "소셜 로그인 사용자의 전화번호 인증을 수행합니다."
	)

	@ApiResponse(responseCode = "200", description = "전화번호 인증 성공")
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
	@PostMapping("/{user_id}/phone-verification")
	public ResponseEntity<Object> verifyPhone(
		@Parameter(description = "유저 ID", example = "1")
		@PathVariable("user_id") Long userId,

		@Valid @RequestBody VerifyCodeRequest request,

		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		if (request.getCode() == null) {
			SmsVerificationResponse resp = memberUtilService.sendCode(request.getPhone());
			return ResponseEntity.ok(resp);
		}
		memberService.verifyPhone(userId, request.getCode(), request, customUserDetails);
		return ResponseEntity.ok().body(Map.of("message", "전화번호 인증이 완료되었습니다."));
	}

	/**
	 * 사용자 마이페이지 조회 API
	 * @param userId 유저 고유 ID
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@Operation(
		summary = "회원 정보 조회 (마이페이지)",
		description = "인증된 사용자의 마이페이지 정보를 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "403", description = "접근 권한 없음")
	@GetMapping("/{user_id}")
	public ResponseEntity<Object> getMember(
		@Parameter(description = "유저 ID", example = "1")
		@PathVariable("user_id") Long userId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		return ResponseEntity.ok(new ProfileResponse(memberService.getMember(userId, customUserDetails)));
	}

	/**
	 * 사용자 마이페이지 수정 API
	 * @param userId 유저 고유 Id
	 * @param customUserDetails 인증된 사용자 정보
	 * @return {@link ResponseEntity}
	 */
	@Operation(
		summary = "회원 정보 수정",
		description = "인증된 사용자의 프로필 정보를 수정합니다."
	)
	@ApiResponse(responseCode = "200", description = "수정 완료")
	@ApiResponse(responseCode = "403", description = "접근 권한 없음")
	@ApiResponse(responseCode = "400", description = "잘못된 요청")
	@PatchMapping("/{user_id}")
	public ResponseEntity<Object> updateMember(
		@Parameter(description = "수정할 회원의 ID", example = "1")
		@PathVariable("user_id") Long userId,

		@Parameter(description = "수정 요청 정보")
		@Valid @RequestBody UpdateProfileRequest req,

		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		UpdateProfileResponse resp = memberService.updateMember(userId, req, customUserDetails);
		return ResponseEntity.ok(resp);
	}

	@Operation(
		summary = "회원 탈퇴",
		description = "인증된 사용자의 회원 정보를 삭제합니다."
	)
	@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
	@ApiResponse(responseCode = "403", description = "접근 권한 없음")
	@DeleteMapping("/{user_id}")
	public ResponseEntity<Object> deleteMember(
		@Parameter(description = "삭제할 회원의 ID", example = "1")
		@PathVariable("user_id") Long userId,

		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		memberService.deleteMember(userId, customUserDetails);
		return ResponseEntity.ok().body(Map.of("message", "회원 탈퇴가 정상적으로 처리되었습니다."));
	}

	@Operation(
		summary = "내 정보 조회",
		description = "현재 로그인된 사용자의 정보를 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	@GetMapping("/me")
	public ResponseEntity<MemberInfoResponse> getCurrentUser(
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
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

	@PatchMapping("/password/{user_id}")
	public ResponseEntity<?> updatePassword(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("user_id") Long userId,
		@Valid @RequestBody PasswordUpdateRequest request) {
		memberService.updatePassword(userDetails, userId, request);
		return ResponseEntity.ok().body(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
	}

	@PostMapping("/toggle/{user_id}")
	public ResponseEntity<?> toggleUserView(
		@PathVariable("user_id") Long userId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		HttpServletResponse resp) {
		memberService.toggleUserView(userId, userDetails, resp);
		return ResponseEntity.ok().body(Map.of("message", "사용자 뷰 상태가 변경되었습니다."));
	}
}

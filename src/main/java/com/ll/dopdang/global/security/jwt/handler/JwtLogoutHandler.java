package com.ll.dopdang.global.security.jwt.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.global.security.jwt.service.TokenService;
import com.ll.dopdang.standard.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtLogoutHandler
 */
@Slf4j
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
	/**
	 * 토큰 서비스
	 */
	private final TokenService tokenService;
	/**
	 * 토큰 관리 서비스
	 */
	private final TokenManagementService tokenManagementService;
	private final JwtUtil jwtUtil;
	private final MemberRepository memberRepository;

	/**
	 * 로그아웃 메서드
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param auth Authentication
	 */
	@Override
	public void logout(HttpServletRequest req, HttpServletResponse resp, Authentication auth) {
		String accessToken = tokenService.getAccessToken(req);

		boolean isLoggedIn = accessToken != null;
		req.setAttribute("isLoggedIn", isLoggedIn);

		if (isLoggedIn) {
			try {
				Long userId = jwtUtil.getUserId(accessToken);
				memberRepository.findById(userId).ifPresent(member -> {
					Member updatedMember = Member.builder()
						.id(member.getId())
						.email(member.getEmail())
						.password(member.getPassword())
						.name(member.getName())
						.profileImage(member.getProfileImage())
						.phone(member.getPhone())
						.status(member.getStatus())
						.userRole(member.getUserRole())
						.memberId(member.getMemberId())
						.uniqueKey(member.getUniqueKey())
						.isClient(true) // isClient를 true로 설정
						.createdAt(member.getCreatedAt())
						.updatedAt(member.getUpdatedAt())
						.build();
					memberRepository.save(updatedMember);
					log.info("사용자 ID: {}의 isClient 값을 true로 변경했습니다.", userId);
				});
			} catch (Exception e) {
				log.error("로그아웃 중 isClient 값 변경 실패: {}", e.getMessage());
			}
			// 토큰 무효화
			tokenManagementService.invalidateTokens(accessToken);

			// 쿠키 무효화
			resp.addCookie(tokenManagementService.invalidateCookie("accessToken"));

			log.info("로그아웃 처리 - 토큰 무효화 완료");
		} else {
			log.info("로그아웃 처리 - 로그인 상태가 아님");
		}
	}
}

package com.ll.dopdang.global.security.jwt.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.global.security.jwt.service.TokenService;

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

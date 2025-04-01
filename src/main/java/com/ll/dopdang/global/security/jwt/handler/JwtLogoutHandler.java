package com.ll.dopdang.global.security.jwt.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.global.security.jwt.service.TokenService;
import com.ll.dopdang.standard.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JwtLogoutHandler
 */
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
	/**
	 * jwt 유틸리티
	 */
	private final JwtUtil jwtUtil;
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
		String authorization = req.getHeader("Authorization");
		String accessToken = null;

		if (authorization != null && authorization.startsWith("Bearer ")) {
			accessToken = authorization.substring(7);
		}

		String refreshToken = tokenService.getRefreshToken(req);

		if (accessToken != null && refreshToken != null) {
			try {
				String username = jwtUtil.getUsername(accessToken);
				tokenManagementService.invalidateTokens(username, accessToken);
			} catch (Exception e) {
				// 예외 처리
			}
		}
	}
}

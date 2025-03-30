package com.ll.dopdang.global.security.jwt.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.global.security.jwt.service.TokenService;
import com.ll.dopdang.standard.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
	private final JwtUtil jwtUtil;
	private final TokenService tokenService;
	private final TokenManagementService tokenManagementService;

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

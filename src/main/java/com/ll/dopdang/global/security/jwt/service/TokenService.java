package com.ll.dopdang.global.security.jwt.service;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * TokenService (토큰 서비스)
 */
@Component
@RequiredArgsConstructor
public class TokenService {
	/**
	 * 리프레시 토큰 가져오는 메서드
	 * @param request HttpServletRequest
	 * @return {@link String}
	 */
	public String getRefreshToken(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}

		String refreshToken = null;

		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals("refreshToken")) {
				refreshToken = cookie.getValue();
			}
		}

		return refreshToken;
	}
}

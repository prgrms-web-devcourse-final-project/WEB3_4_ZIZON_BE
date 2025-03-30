package com.ll.dopdang.global.security.jwt.service;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenService {
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

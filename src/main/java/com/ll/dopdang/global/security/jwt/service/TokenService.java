package com.ll.dopdang.global.security.jwt.service;

import org.springframework.stereotype.Component;

import com.ll.dopdang.global.redis.repository.RedisRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * TokenService (토큰 서비스)
 */
@Component
@RequiredArgsConstructor
public class TokenService {
	private final RedisRepository redisRepository;

	/**
	 * 리프레시 토큰 가져오는 메서드
	 * @param accessToken 엑세스 토큰
	 * @return {@link String}
	 */
	public String getRefreshToken(String accessToken) {
		if (accessToken == null) {
			return null;
		}
		return (String)redisRepository.get(accessToken);
	}

	public String getAccessToken(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}

		String accessToken = null;

		for (Cookie cookie : request.getCookies()) {
			if ("accessToken".equals(cookie.getName())) {
				accessToken = cookie.getValue();
				break;
			}
		}

		return accessToken;
	}
}

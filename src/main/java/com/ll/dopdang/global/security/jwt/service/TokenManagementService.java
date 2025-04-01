package com.ll.dopdang.global.security.jwt.service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.standard.util.AuthResponseUtil;
import com.ll.dopdang.standard.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TokenManagementService (토큰 관리 서비스)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenManagementService {
	/**
	 * jwt 유틸리티
	 */
	private final JwtUtil jwtUtil;
	/**
	 * redis repository
	 */
	private final RedisRepository redisRepository;
	/**
	 * 토큰 서비스
	 */
	private final TokenService tokenService;
	/**
	 * ObjectMapper
	 */
	private final ObjectMapper objectMapper;

	/**
	 * 엑세스 토큰 유효 기간
	 */
	@Value("${jwt.token.access-expiration}")
	private long accessExpiration;

	/**
	 * 리프레시 토큰 유효 기간
	 */
	@Value("${jwt.token.refresh-expiration}")
	private long refreshExpiration;

	/**
	 * 엑세스 토큰
	 */
	@Getter
	private String accessToken;

	/**
	 * 리프레시 토큰
	 */
	@Getter
	private Cookie refreshTokenCookie;

	/**
	 * 토큰 생성 및 저장
	 * @param userDetails CustomUserDetails
	 * @param response HttpServletResponse
	 */
	public void createAndStoreTokens(CustomUserDetails userDetails, HttpServletResponse response) {
		String accessToken = jwtUtil.createAccessToken(userDetails, accessExpiration);
		String refreshToken = jwtUtil.createRefreshToken(userDetails, refreshExpiration);

		Cookie accessTokenCookie = jwtUtil.setJwtCookie("accessToken", accessToken, accessExpiration);
		response.addCookie(accessTokenCookie);

		redisRepository.save(accessToken, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);

		this.accessToken = accessToken;
	}

	/**
	 * 토큰 재발급
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws IOException 예외
	 */
	public void reissueTokens(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String accessToken = tokenService.getAccessToken(request);
		String refreshToken = tokenService.getRefreshToken(accessToken);

		if (refreshToken == null) {
			throw new IllegalArgumentException("리프레시 토큰이 없습니다.");
		}

		String username = jwtUtil.getUsername(refreshToken);
		String role = jwtUtil.getRole(refreshToken);

		CustomUserDetails userDetails = new CustomUserDetails(
			Member.builder()
				.email(username)
				.userRole(role)
				.id(jwtUtil.getUserId(refreshToken))
				.build()
		);

		// 새 토큰 발급
		String newAccessToken = jwtUtil.createAccessToken(userDetails, accessExpiration);
		String newRefreshToken = jwtUtil.createRefreshToken(userDetails, refreshExpiration);

		Cookie newAccessTokenCookie = jwtUtil.setJwtCookie("accessToken", newAccessToken, accessExpiration);
		response.addCookie(newAccessTokenCookie);

		// Redis 업데이트
		redisRepository.remove(accessToken); // 이전 액세스 토큰 제거
		redisRepository.save(newAccessToken, newRefreshToken, refreshExpiration, TimeUnit.MILLISECONDS);

		// 응답 처리
		AuthResponseUtil.success(
			response,
			newAccessToken,
			null, // refreshToken은 쿠키가 아닌 Redis에 저장
			HttpServletResponse.SC_OK,
			ResponseEntity.ok("AccessToken 발급 성공"),
			objectMapper);

		log.info("토큰 재발급 성공 - 사용자: {}", username);
	}

	/**
	 * 토큰 무효화
	 * @param accessToken 액세스 토큰
	 */
	public void invalidateTokens(String accessToken) {
		if (accessToken == null) {
			return;
		}

		try {
			// 사용자 정보 추출
			String username = jwtUtil.getUsername(accessToken);

			// Redis에서 리프레시 토큰 삭제
			redisRepository.remove(accessToken);

			// 액세스 토큰 블랙리스트에 추가 (만료 시간까지만)
			long expiration = jwtUtil.getExpirationDate(accessToken).getTime() - System.currentTimeMillis();
			if (expiration > 0) {
				redisRepository.save("blacklist:" + accessToken, "LOGOUT", expiration, TimeUnit.MILLISECONDS);
			}

			log.info("토큰 무효화 성공 - 사용자: {}", username);
		} catch (Exception e) {
			log.error("토큰 무효화 중 오류 발생", e);
		}
	}

	/**
	 * 쿠키 무효화
	 * @param cookieName 쿠키 이름
	 * @return 무효화된 쿠키
	 */
	public Cookie invalidateCookie(String cookieName) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		return cookie;
	}
}

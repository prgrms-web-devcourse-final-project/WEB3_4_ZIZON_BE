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

@Service
@RequiredArgsConstructor
public class TokenManagementService {
	private final JwtUtil jwtUtil;
	private final RedisRepository redisRepository;
	private final TokenService tokenService;
	private final ObjectMapper objectMapper;

	@Value("${jwt.token.access-expiration}")
	private long accessExpiration;

	@Value("${jwt.token.refresh-expiration}")
	private long refreshExpiration;

	@Getter
	private String accessToken;

	@Getter
	private Cookie refreshTokenCookie;

	public void createAndStoreTokens(CustomUserDetails userDetails, HttpServletResponse response) {
		String accessToken = jwtUtil.createAccessToken(userDetails, accessExpiration);
		String refreshToken = jwtUtil.createRefreshToken(userDetails, refreshExpiration);

		Cookie accessTokenCookie = jwtUtil.setJwtCookie("accessToken", accessToken, accessExpiration);
		response.addCookie(accessTokenCookie);

		redisRepository.remove(accessToken);
		redisRepository.save(accessToken, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);

		this.accessToken = accessToken;
	}

	public void reissueTokens(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String refreshToken = tokenService.getRefreshToken(request);

		if (refreshToken == null) {
			throw new IllegalArgumentException("리프레시 토큰이 없습니다.");
		}

		String username = jwtUtil.getUsername(refreshToken);
		String role = jwtUtil.getRole(refreshToken);

		// Redis에 저장된 리프레시 토큰과 비교
		if (!redisRepository.get(username).equals(refreshToken)) {
			throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
		}

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

		// Redis 업데이트
		redisRepository.remove(userDetails.getUsername());
		redisRepository.save(userDetails.getUsername(), newRefreshToken, refreshExpiration, TimeUnit.MILLISECONDS);

		// 응답 처리
		AuthResponseUtil.success(
			response,
			newAccessToken,
			jwtUtil.setJwtCookie("refreshToken", newRefreshToken, refreshExpiration),
			HttpServletResponse.SC_OK,
			ResponseEntity.ok("AccessToken 발급 성공"),
			objectMapper);
	}

	public void invalidateTokens(String username, String accessToken) {
		if (accessToken != null) {
			try {
				// 액세스 토큰 블랙리스트에 추가
				long expiration = jwtUtil.getExpirationDate(accessToken).getTime() - System.currentTimeMillis();
				redisRepository.save(accessToken, "Logout", expiration, TimeUnit.MILLISECONDS);

				// 리프레시 토큰 삭제
				redisRepository.remove(username);
			} catch (Exception e) {
				// 예외 처리
			}
		}
	}
}

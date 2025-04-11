package com.ll.dopdang.standard.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;

/**
 * jwt 관련 유틸리티
 */
@Component
public class JwtUtil {
	/**
	 * jwt 시크릿 키
	 */
	private final SecretKey secretKey;

	/**
	 * 시크릿 키 부여
	 * @param secret 시크릿
	 */
	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS512.key().build().getAlgorithm());
	}

	/**
	 * 토큰에서 ID 추출
	 * @param token 토큰
	 * @return {@link Long}
	 */
	public Long getUserId(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("id", Long.class);
	}

	/**
	 * 토큰에서 이메일 추출
	 * @param token 토큰
	 * @return {@link String}
	 */
	public String getUsername(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("username", String.class);
	}

	/**
	 * 토큰에서 역할 추출
	 * @param token 토큰
	 * @return {@link String}
	 */
	public String getRole(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("role", String.class);
	}

	/**
	 * 토큰에서 isClient 값 추출
	 * @param token 토큰
	 * @return {@link Boolean}
	 */
	public boolean getIsClient(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("isClient", Boolean.class);
	}

	/**
	 * 토큰이 만료됐는지 확인
	 * @param token 토큰
	 * @return {@link Boolean}
	 */
	public boolean isExpired(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration()
			.before(new Date());
	}

	/**
	 * 토큰의 만료일 추출
	 * @param token 토큰
	 * @return {@link Date}
	 */
	public Date getExpirationDate(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration();
	}

	/**
	 * 엑세스 토큰 생성
	 * @param customUserDetails 인증 유저 정보
	 * @param expiration 만료 기간
	 * @return {@link String}
	 */
	public String createAccessToken(CustomUserDetails customUserDetails, long expiration) {
		long currentTime = System.currentTimeMillis();

		return Jwts.builder()
			.claim("subject", "access")
			.claim("id", customUserDetails.getMember().getId())
			.claim("username", customUserDetails.getUsername())
			.claim("role", customUserDetails.getMember().getUserRole())
			.claim("status", customUserDetails.getMember().getStatus())
			.claim("isClient", customUserDetails.getMember().isClient())
			.issuedAt(new Date(currentTime))
			.expiration(new Date(currentTime + expiration))
			.signWith(secretKey)
			.compact();
	}

	/**
	 * 리프레시 토큰 생성
	 * @param customUserDetails 인증 유저 정보
	 * @param expiration 만료 기간
	 * @return {@link String}
	 */
	public String createRefreshToken(CustomUserDetails customUserDetails, long expiration) {
		long currentTime = System.currentTimeMillis();

		return Jwts.builder()
			.claim("subject", "refresh")
			.claim("id", customUserDetails.getMember().getId())
			.claim("username", customUserDetails.getUsername())
			.claim("role", customUserDetails.getMember().getUserRole())
			.claim("status", customUserDetails.getMember().getStatus())
			.claim("isClient", customUserDetails.getMember().isClient())
			.issuedAt(new Date(currentTime))
			.expiration(new Date(currentTime + expiration))
			.signWith(secretKey)
			.compact();
	}

	/**
	 * 쿠키 설정
	 * @param key key 값
	 * @param value value 값
	 * @param expiration 만료 기간
	 * @return {@link Cookie}
	 */
	public String setJwtCookie(String key, String value, long expiration) {
		return ResponseCookie.from(key, value)
			.path("/")
			.sameSite("None")
			.secure(true)
			.domain(".dopdang.shop")
			.maxAge(expiration / 1000)
			.httpOnly(false)
			.build()
			.toString();
	}
}

package com.ll.dopdang.standard.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;

@Component
public class JwtUtil {
	private final SecretKey secretKey;

	public JwtUtil(@Value("${jwt.secret}") String secret) {
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS512.key().build().getAlgorithm());
	}

	public Long getUserId(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("id", Long.class);
	}

	public String getUsername(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("username", String.class);
	}

	public String getRole(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("role", String.class);
	}

	public boolean isExpired(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration()
			.before(new Date());
	}

	public Date getExpirationDate(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration();
	}

	public String createAccessToken(CustomUserDetails customUserDetails, long expiration) {
		long currentTime = System.currentTimeMillis();

		return Jwts.builder()
			.claim("subject", "access")
			.claim("id", customUserDetails.getMember().getId())
			.claim("username", customUserDetails.getUsername())
			.claim("role", customUserDetails.getMember().getUserRole())
			.claim("status", customUserDetails.getMember().getStatus())
			.issuedAt(new Date(currentTime))
			.expiration(new Date(currentTime + expiration))
			.signWith(secretKey)
			.compact();
	}

	public String createRefreshToken(CustomUserDetails customUserDetails, long expiration) {
		long currentTime = System.currentTimeMillis();

		return Jwts.builder()
			.claim("subject", "refresh")
			.claim("id", customUserDetails.getMember().getId())
			.claim("username", customUserDetails.getUsername())
			.claim("role", customUserDetails.getMember().getUserRole())
			.issuedAt(new Date(currentTime))
			.expiration(new Date(currentTime + expiration))
			.signWith(secretKey)
			.compact();
	}

	public Cookie setJwtCookie(String key, String value, long expiration) {

		Cookie cookie = new Cookie(key, value);
		cookie.setMaxAge((int)expiration / 1000);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setHttpOnly(true);

		return cookie;
	}
}

package com.ll.dopdang.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.global.security.SecurityConfig;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.standard.util.AuthResponseUtil;
import com.ll.dopdang.standard.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JwtAuthorizationFilter
 */
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
	/**
	 * jwt 유틸리티
	 */
	private final JwtUtil jwtUtil;
	/**
	 * 토큰 관리 서비스
	 */
	private final TokenManagementService tokenManagementService;
	/**
	 * ObjectMapper
	 */
	private final ObjectMapper objectMapper;

	/**
	 *
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param filterChain FilterChain
	 * @throws ServletException 예외
	 * @throws IOException 예외
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws
		ServletException,
		IOException {
		String requestUri = req.getRequestURI();

		if (requestUri.equals("/api/v1/reissue")) {
			handleTokenReissue(req, resp);
		} else {
			handleAccessTokenValidation(req, resp, filterChain);
		}
	}

	/**
	 * 토큰 재발급
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @throws IOException 예외
	 */
	private void handleTokenReissue(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			tokenManagementService.reissueTokens(req, resp);
		} catch (Exception e) {
			AuthResponseUtil.failLogin(
				resp,
				ResponseEntity.badRequest().body(e.getMessage()),
				HttpServletResponse.SC_BAD_REQUEST,
				objectMapper);
		}
	}

	/**
	 * 엑세스 토큰 검증
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param filterChain FilterChain
	 * @throws ServletException 예외
	 * @throws IOException 예외
	 */
	private void handleAccessTokenValidation(HttpServletRequest req, HttpServletResponse resp,
		FilterChain filterChain) throws ServletException, IOException {
		if (isPublicUrl(req)) {
			filterChain.doFilter(req, resp);
			return;
		}

		String token = extractToken(req);

		if (token == null) {
			AuthResponseUtil.failLogin(
				resp,
				ResponseEntity.badRequest().build(),
				HttpServletResponse.SC_UNAUTHORIZED,
				objectMapper);
			return;
		}

		try {
			jwtUtil.isExpired(token);
			String username = jwtUtil.getUsername(token);
			String role = jwtUtil.getRole(token);
			Long userId = jwtUtil.getUserId(token);

			CustomUserDetails userDetails = new CustomUserDetails(
				Member.builder()
					.id(userId)
					.email(username)
					.userRole(role)
					.build()
			);

			Authentication authentication = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				userDetails.getAuthorities()
			);

			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(req, resp);
		} catch (ExpiredJwtException e) {
			AuthResponseUtil.failLogin(
				resp,
				ResponseEntity.badRequest().build(),
				HttpServletResponse.SC_UNAUTHORIZED,
				objectMapper);
		} catch (JwtException e) {
			AuthResponseUtil.failLogin(
				resp,
				ResponseEntity.badRequest().build(),
				HttpServletResponse.SC_UNAUTHORIZED,
				objectMapper);
		} catch (Exception e) {
			AuthResponseUtil.failLogin(
				resp,
				ResponseEntity.badRequest().build(),
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				objectMapper);
		}
	}

	/**
	 * 전체 허용 URL인지 검증
	 * @param request HttpServletRequest
	 * @return {@link Boolean}
	 */
	private boolean isPublicUrl(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		HttpMethod method = HttpMethod.valueOf(request.getMethod());

		var patterns = SecurityConfig.getPublicUrls().get(method);
		if (patterns == null) {
			return false;
		}

		return patterns.stream()
			.anyMatch(pattern -> new AntPathMatcher().match(pattern, requestUri));
	}

	/**
	 * 토큰 추출
	 * @param request HttpServletRequest
	 * @return {@link String}
	 */
	private String extractToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("accessToken".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
}

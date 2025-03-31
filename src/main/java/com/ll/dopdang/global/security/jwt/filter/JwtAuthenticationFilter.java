package com.ll.dopdang.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.dto.request.LoginRequest;
import com.ll.dopdang.domain.member.dto.response.LoginResponse;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.standard.util.AuthResponseUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JwtAuthenticationFilter
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	/**
	 * 토큰 관리 서비스
	 */
	private final TokenManagementService tokenManagementService;
	/**
	 * ObjectMapper
	 */
	private final ObjectMapper objectMapper;
	/**
	 * AuthenticationManager
	 */
	private final AuthenticationManager authenticationManager;

	/**
	 *
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @return {@link Authentication}
	 * @throws AuthenticationException 예외
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse resp) throws
		AuthenticationException {
		try {
			LoginRequest loginRequest = objectMapper.readValue(req.getInputStream(), LoginRequest.class);

			String email = loginRequest.getEmail();
			String password = loginRequest.getPassword();

			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

			return authenticationManager.authenticate(authToken);
		} catch (IOException e) {
			throw new AuthenticationServiceException("잘못된 로그인 정보입니다.");
		}
	}

	/**
	 *
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param failed AuthenticationException
	 * @throws IOException 예외
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse resp,
		AuthenticationException failed) throws IOException {
		AuthResponseUtil.failLogin(
			resp,
			(ResponseEntity<?>)ResponseEntity.badRequest(),
			HttpServletResponse.SC_UNAUTHORIZED,
			objectMapper
		);
	}

	/**
	 *
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param chain FilterChain
	 * @param authentication Authentication
	 * @throws IOException 예외
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse resp, FilterChain chain,
		Authentication authentication) throws IOException {
		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

		// 토큰 관리 서비스에 위임
		tokenManagementService.createAndStoreTokens(userDetails, resp);

		AuthResponseUtil.success(
			resp,
			tokenManagementService.getAccessToken(),
			tokenManagementService.getRefreshTokenCookie(),
			HttpServletResponse.SC_OK,
			ResponseEntity.ok(new LoginResponse(userDetails.getMember().getName(), userDetails.getUsername(),
				userDetails.getMember().getProfileImage())),
			objectMapper
		);
	}
}

package com.ll.dopdang.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

			// 이메일 검증
			if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
				throw new AuthenticationServiceException("이메일을 입력해주세요.");
			}
			if (!loginRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
				throw new AuthenticationServiceException("올바른 이메일 형식이 아닙니다.");
			}

			// 비밀번호 검증
			if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
				throw new AuthenticationServiceException("비밀번호를 입력해주세요.");
			}
			if (!loginRequest.getPassword().matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
				throw new AuthenticationServiceException("비밀번호는 특수문자를 포함해야 합니다.");
			}

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
		ResponseEntity<?> responseEntity;
		int status;
		String message = failed.getMessage();

		if (failed instanceof AuthenticationServiceException) {
			// 이메일/비밀번호 형식 검증 실패
			status = HttpServletResponse.SC_BAD_REQUEST;
			responseEntity = ResponseEntity.badRequest().body(message);
		} else if (message != null && message.startsWith("DEACTIVATED:")) {
			// 탈퇴한 계정 처리 - 명확한 마커로 식별
			status = HttpServletResponse.SC_FORBIDDEN;
			responseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body("탈퇴한 계정입니다.");
		} else if (failed instanceof UsernameNotFoundException) {
			// 존재하지 않는 계정
			status = HttpServletResponse.SC_UNAUTHORIZED;
			responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("존재하지 않는 계정입니다.");
		} else {
			// 그 외 인증 실패
			status = HttpServletResponse.SC_UNAUTHORIZED;
			responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("이메일 또는 비밀번호가 일치하지 않습니다.");
		}

		AuthResponseUtil.failLogin(resp, responseEntity, status, objectMapper);
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

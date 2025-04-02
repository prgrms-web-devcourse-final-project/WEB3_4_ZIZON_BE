package com.ll.dopdang.global.security.jwt.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JwtLogoutSuccessHandler
 */
@RequiredArgsConstructor
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {
	/**
	 * ObjectMapper
	 */
	private final ObjectMapper objectMapper;

	/**
	 * 로그아웃 성공을 다루는 메서드
	 * @param req HttpServletRequest
	 * @param resp HttpServletResponse
	 * @param auth Authentication
	 * @throws IOException 예외
	 * @throws ServletException 예외
	 */
	@Override
	public void onLogoutSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws
		IOException, ServletException {
		Map<String, Object> response = new HashMap<>();

		boolean isLoggedIn = req.getAttribute("isLoggedIn") != null && (boolean)req.getAttribute("isLoggedIn");

		if (isLoggedIn) {
			// 정상적인 로그아웃 처리
			response.put("code", "200");
			response.put("message", "로그아웃 되었습니다.");
			resp.setStatus(HttpStatus.OK.value());
		} else {
			// 이미 로그아웃 상태
			response.put("code", "400");
			response.put("message", "이미 로그아웃 상태입니다.");
			resp.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(objectMapper.writeValueAsString(response));
	}
}

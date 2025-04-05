package com.ll.dopdang.standard.util;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 인증 인가 반환값 유틸리티
 */
public class AuthResponseUtil {
	/**
	 * 성공 응답값
	 * @param response HttpServletResponse
	 * @param accessToken 엑세스 토큰
	 * @param cookie 쿠키
	 * @param status 상태 코드
	 * @param rsData 결과 데이터
	 * @param om ObjectMapper
	 * @throws IOException 예외
	 */
	public static void success(HttpServletResponse response, String accessToken, Cookie cookie, int status,
		ResponseEntity<?> rsData, ObjectMapper om) throws
		IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(om.writeValueAsString(rsData.getBody()));
	}

	/**
	 * 실패 응답값
	 * @param response HttpServletResponse
	 * @param rsData 결과 데이터
	 * @param status 상태 코드
	 * @param om ObjectMapper
	 * @throws IOException 예외
	 */
	public static void failLogin(HttpServletResponse response, ResponseEntity<?> rsData, int status,
		ObjectMapper om) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(status);
		response.getWriter().write(om.writeValueAsString(rsData));
	}
}

package com.ll.dopdang.standard.util;

import java.io.IOException;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class AuthResponseUtil {
	public static void success(HttpServletResponse response, String accessToken, Cookie cookie, int status,
		ResponseEntity<?> rsData, ObjectMapper om) throws
		IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(om.writeValueAsString(rsData));
	}

	public static void failLogin(HttpServletResponse response, ResponseEntity<?> rsData, int status,
		ObjectMapper om) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(status);
		response.getWriter().write(om.writeValueAsString(rsData));
	}
}

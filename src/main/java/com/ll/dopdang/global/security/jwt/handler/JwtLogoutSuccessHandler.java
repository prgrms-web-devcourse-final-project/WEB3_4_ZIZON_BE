package com.ll.dopdang.global.security.jwt.handler;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {
	private final ObjectMapper objectMapper;

	@Override
	public void onLogoutSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth) throws
		IOException, ServletException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(objectMapper.writeValueAsString(ResponseEntity.ok()));
	}
}

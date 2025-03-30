package com.ll.dopdang.global.security.oauth2.handler;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.standard.util.AuthResponseUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse resp,
		AuthenticationException exception) throws IOException {
		AuthResponseUtil.failLogin(
			resp,
			(ResponseEntity<?>)ResponseEntity.badRequest(),
			HttpServletResponse.SC_BAD_REQUEST,
			objectMapper
		);
	}
}

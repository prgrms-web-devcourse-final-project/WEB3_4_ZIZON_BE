package com.ll.dopdang.global.security.jwt.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.standard.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenValidationService {
	private final JwtUtil jwtUtil;
	private final RedisRepository redisRepository;
	private final MemberRepository memberRepository;

	public Authentication validateTokenAndGetAuthentication(String token) {
		jwtUtil.isExpired(token);

		String username = jwtUtil.getUsername(token);

		String refreshToken = (String)redisRepository.get(token);
		if (refreshToken == null) {
			throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
		}

		Member member = memberRepository.findByEmail(username)
			.orElseThrow(() -> {
				return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
			});

		CustomUserDetails userDetails = new CustomUserDetails(member);

		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
}

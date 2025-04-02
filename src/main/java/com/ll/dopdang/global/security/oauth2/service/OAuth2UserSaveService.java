package com.ll.dopdang.global.security.oauth2.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.security.oauth2.dto.OAuthAttributes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2UserSaveService {
	private final MemberRepository memberRepository;

	@Transactional
	public Member saveIfNotExist(OAuthAttributes attributes) {
		Member member = null;

		// memberId로 사용자 조회
		if (attributes.getMemberId() != null) {
			member = memberRepository.findByMemberId(attributes.getMemberId()).orElse(null);
		}

		// 이메일로 사용자 조회
		if (member == null && attributes.getEmail() != null) {
			member = memberRepository.findByEmail(attributes.getEmail()).orElse(null);
		}

		if (member == null) {
			member = attributes.toEntity();
			member = memberRepository.save(member);
		}

		return member;
	}
}

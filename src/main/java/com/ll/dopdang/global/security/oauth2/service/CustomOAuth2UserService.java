package com.ll.dopdang.global.security.oauth2.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.security.oauth2.dto.OAuthAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소셜 로그인 유저 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	/**
	 * 소셜 유저 변환 서비스
	 */
	private final OAuthUserConverter oAuthUserConverter;
	/**
	 * 유저 레포지터리
	 */
	private final MemberRepository memberRepository;

	/**
	 * 소셜 로그인 유저의 정보를 Member 테이블에 맞게 변환
	 * @param userRequest 소셜 로그인 유저 request
	 * @return {@link DefaultOAuth2User}
	 * @throws OAuth2AuthenticationException 소셜 로그인 정보 처리 오류
	 */
	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);

		try {
			String registrationId = userRequest.getClientRegistration().getRegistrationId();
			String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

			OAuthAttributes attributes = oAuthUserConverter.convert(registrationId, userNameAttributeName,
				oauth2User.getAttributes());

			Member user = saveOrUpdate(attributes);
			log.info("OAuth2 로그인 사용자 정보: id={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());

			Map<String, Object> updatedAttributes = new HashMap<>(attributes.getAttributes());
			updatedAttributes.put("id", user.getId());

			return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority(user.getUserRole())),
				updatedAttributes,
				userNameAttributeName
			);
		} catch (Exception e) {
			log.error("OAuth2 사용자 정보 처리 중 오류 발생: {}", e.getMessage(), e);
			OAuth2Error oauth2Error = new OAuth2Error(
				"user_mapping_error",
				"OAuth2 사용자 정보 처리 중 오류가 발생했습니다: " + e.getMessage(),
				null
			);
			throw new OAuth2AuthenticationException(oauth2Error, e);
		}
	}

	/**
	 * 소셜 로그인 시 정보를 저장 혹은 업데이트 하는 메서드
	 * @param attributes 유저 정보
	 * @return {@link Member}
	 */
	@Transactional
	protected Member saveOrUpdate(OAuthAttributes attributes) {
		Member user = null;

		if ("kakao".equals(attributes.getProvider()) && attributes.getMemberId() != null) {
			user = memberRepository.findByMemberId(attributes.getMemberId())
				.map(entity -> entity.update(attributes.getName(), attributes.getProfileImage()))
				.orElse(null);
		} else if ("google".equals(attributes.getProvider()) && attributes.getMemberId() != null) {
			user = memberRepository.findByMemberId(attributes.getMemberId())
				.map(entity -> entity.update(attributes.getName(), attributes.getProfileImage()))
				.orElse(null);
		} else if ("naver".equals(attributes.getProvider()) && attributes.getMemberId() != null) {
			user = memberRepository.findByMemberId(attributes.getMemberId())
				.map(entity -> entity.update(attributes.getName(), attributes.getProfileImage()))
				.orElse(null);
		}

		if (user == null && attributes.getEmail() != null) {
			user = memberRepository.findByEmail(attributes.getEmail())
				.map(entity -> {
					if ("kakao".equals(attributes.getProvider()) && entity.getMemberId() == null) {
						entity.assignMemberId(attributes.getMemberId());
					} else if ("google".equals(attributes.getProvider()) && entity.getMemberId() == null) {
						entity.assignMemberId(attributes.getMemberId());
					} else if ("naver".equals(attributes.getProvider()) && entity.getMemberId() == null) {
						entity.assignMemberId(attributes.getMemberId());
					}
					return entity.update(attributes.getName(), attributes.getProfileImage());
				})
				.orElse(null);
		}

		if (user == null) {
			user = attributes.toEntity();
		}

		Member savedUser = memberRepository.save(user);
		return savedUser;
	}
}

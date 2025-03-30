package com.ll.dopdang.global.security.oauth2.service;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import com.ll.dopdang.global.security.oauth2.dto.OAuthAttributes;

@Component
public class OAuthUserConverter {
	/**
	 * 소셜 로그인 제공자 타입에 따라 적절한 변환 메서드 호출
	 * @param registrationId 소셜 로그인 제공자
	 * @param userNameAttributeName 소셜 로그인 고유 ID
	 * @param attributes 소셜 로그인 유저 정보
	 * @return {@link OAuthAttributes}
	 */
	public OAuthAttributes convert(String registrationId, String userNameAttributeName,
		Map<String, Object> attributes) {
		if ("kakao".equals(registrationId)) {
			return ofKakao(userNameAttributeName, attributes);
		} else if ("google".equals(registrationId)) {
			return ofGoogle(userNameAttributeName, attributes);
		} else if ("naver".equals(registrationId)) {
			return ofNaver(userNameAttributeName, attributes);
		}
		throw new OAuth2AuthenticationException("해당 소셜 로그인은 지원하지 않습니다.");
	}

	/**
	 * 카카오 유저 변환
	 * @param userNameAttributeName 소셜 로그인 고유 ID
	 * @param attributes 소셜 로그인 유저 정보
	 * @return {@link OAuthAttributes}
	 */
	private OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
		Map<String, Object> properties = (Map<String, Object>)attributes.get("properties");

		return OAuthAttributes.builder()
			.name((String)properties.get("nickname"))
			.profileImage((String)properties.get("profile_image"))
			.memberId(String.valueOf(attributes.get(userNameAttributeName)))
			.email(null)
			.provider("kakao")
			.attributes(attributes)
			.build();
	}

	/**
	 * 구글 유저 변환
	 * @param userNameAttributeName 소셜 로그인 고유 ID
	 * @param attributes 소셜 로그인 유저 정보
	 * @return {@link OAuthAttributes}
	 */
	private OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
		return OAuthAttributes.builder()
			.name((String)attributes.get("name"))
			.profileImage((String)attributes.get("picture"))
			.email((String)attributes.get("email"))
			.memberId(String.valueOf(attributes.get(userNameAttributeName)))
			.provider("google")
			.attributes(attributes)
			.build();
	}

	/**
	 * 네이버 유저 변환
	 * @param userNameAttributeName 소셜 로그인 고유 ID
	 * @param attributes 소셜 로그인 유저 정보
	 * @return {@link OAuthAttributes}
	 */
	private OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");

		return OAuthAttributes.builder()
			.name((String)response.get("name"))
			.profileImage((String)response.get("profile_image"))
			.email((String)response.get("email"))
			.memberId((String)response.get("id"))
			.provider("naver")
			.attributes(attributes)
			.build();
	}
}

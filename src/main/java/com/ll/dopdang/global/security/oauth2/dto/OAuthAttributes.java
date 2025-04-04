package com.ll.dopdang.global.security.oauth2.dto;

import java.util.Map;
import java.util.UUID;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 소셜 로그인 유저 정보 dto
 */
@Getter
@ToString(exclude = "attributes")
public class OAuthAttributes {
	/**
	 * 이름
	 */
	private final String name;
	/**
	 * 프로필 사진
	 */
	private final String profileImage;
	/**
	 * 소셜 ID
	 */
	private final String memberId;
	/**
	 * 이메일
	 */
	private final String email;
	/**
	 * 소셜 로그인 제공자
	 */
	private final String provider;
	/**
	 * 소셜 로그인 유저 정보
	 */
	private final Map<String, Object> attributes;

	/**
	 *
	 * @param name 이름
	 * @param profileImage 프로필 사진
	 * @param memberId 소셜 ID
	 * @param email 이메일
	 * @param provider 소셜 로그인 제공자
	 * @param attributes 소셜 로그인 유저 정보
	 */
	@Builder
	public OAuthAttributes(String name, String profileImage, String memberId, String email, String provider,
		Map<String, Object> attributes) {
		this.name = name;
		this.profileImage = profileImage;
		this.memberId = memberId;
		this.email = email;
		this.provider = provider;
		this.attributes = attributes;
	}

	/**
	 * 엔티티로 변환
	 * @return {@link Member}
	 */
	public Member toEntity() {
		String userEmail = email;
		if (userEmail == null) {
			userEmail = memberId + "@" + provider + ".com";
		}

		return Member.builder()
			.name(name)
			.email(userEmail)
			.password(UUID.randomUUID().toString())
			.phone("")
			.profileImage(profileImage)
			.memberId(memberId)
			.userRole(MemberRole.CLIENT.toString())
			.status(MemberStatus.UNVERIFIED.toString())
			.build();
	}
}

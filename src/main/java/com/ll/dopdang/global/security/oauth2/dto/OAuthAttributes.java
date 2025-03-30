package com.ll.dopdang.global.security.oauth2.dto;

import java.util.Map;
import java.util.UUID;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(exclude = "attributes")
public class OAuthAttributes {
	private String name;
	private String profileImage;
	private String memberId;
	private String email;
	private String provider;
	private Map<String, Object> attributes;

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

	public Member toEntity() {
		String userEmail = email;
		if (userEmail == null) {
			userEmail = name + "@" + provider + ".com";
		}

		return Member.builder()
			.name(name)
			.email(userEmail)
			.password(UUID.randomUUID().toString())
			.phone("")
			.profileImage(profileImage)
			.memberId(memberId)
			.userRole(MemberRole.ROLE_CLIENT.toString())
			.status(MemberStatus.UNCERIFIED.toString())
			.build();
	}
}

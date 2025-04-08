package com.ll.dopdang.domain.member.dto.response;

import com.ll.dopdang.domain.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 시, 보여지는 값 dto
 */
@Getter
@Builder
public class SocialLoginResponse {
	private Long id;
	private String email;
	private String name;
	private String profileImage;
	private String status;
	private Long expertId;
	private String phone;

	public static SocialLoginResponse of(Member member) {
		return SocialLoginResponse.builder()
			.id(member.getId())
			.email(member.getEmail())
			.name(member.getName())
			.profileImage(member.getProfileImage())
			.status(member.getStatus())
			.expertId(member.getExpert() != null ? member.getExpert().getId() : null)
			.phone(member.getPhone() != null ? member.getPhone() : null)
			.build();
	}
}

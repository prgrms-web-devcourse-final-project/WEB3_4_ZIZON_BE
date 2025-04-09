package com.ll.dopdang.domain.member.dto.response;

import com.ll.dopdang.domain.member.entity.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateProfileResponse {
	private String name;
	private String profileImage;

	public static UpdateProfileResponse of(Member member) {
		return UpdateProfileResponse.builder()
			.name(member.getName())
			.profileImage(member.getProfileImage())
			.build();
	}
}

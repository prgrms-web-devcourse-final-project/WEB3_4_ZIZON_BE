package com.ll.dopdang.domain.member.dto.response;

import com.ll.dopdang.domain.member.entity.Member;

import lombok.Getter;

@Getter
public class ProfileResponse {
	private final String name;
	private final String email;
	private final String phone;
	private final String profileImage;

	public ProfileResponse(Member member) {
		this.name = member.getName();
		this.email = member.getEmail();
		this.phone = member.getPhone();
		this.profileImage = member.getProfileImage();
	}
}

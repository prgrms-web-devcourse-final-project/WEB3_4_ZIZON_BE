package com.ll.dopdang.domain.member.dto.response;

import com.ll.dopdang.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 로그인 시, 보여지는 데이터 dto
 */
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
	private Long id;
	private String name;
	private String email;
	private String profileImage;
	private String status;
	private Long expertId;

	public static LoginResponse of(Member member) {
		return LoginResponse.builder()
			.id(member.getId())
			.name(member.getName())
			.email(member.getEmail())
			.profileImage(member.getProfileImage())
			.status(member.getStatus())
			.expertId(member.getExpert() != null ? member.getExpert().getId() : null)
			.build();
	}
}

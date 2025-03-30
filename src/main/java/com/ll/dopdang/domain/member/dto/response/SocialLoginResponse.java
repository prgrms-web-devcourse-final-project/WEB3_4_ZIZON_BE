package com.ll.dopdang.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginResponse {
	private Long id;
	private String email;
	private String name;
	private String profileImage;
	private String status;
}

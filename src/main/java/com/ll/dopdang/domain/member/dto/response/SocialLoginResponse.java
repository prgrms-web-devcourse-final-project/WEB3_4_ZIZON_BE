package com.ll.dopdang.domain.member.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 시, 보여지는 값 dto
 */
@Getter
@Builder
public class SocialLoginResponse {
	/**
	 * 고유 ID
	 */
	private Long id;
	/**
	 * 이메일
	 */
	private String email;
	/**
	 * 이름
	 */
	private String name;
	/**
	 * 프로필 사진
	 */
	private String profileImage;
	/**
	 * 유저 상태
	 */
	private String status;
}

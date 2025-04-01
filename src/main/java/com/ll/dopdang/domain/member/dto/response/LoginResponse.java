package com.ll.dopdang.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 시, 보여지는 데이터 dto
 */
@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
	/**
	 * 이름
	 */
	private String name;
	/**
	 * 이메일
	 */
	private String email;
	/**
	 * 프로필 사진
	 */
	private String profileImage;
}

package com.ll.dopdang.domain.member.dto.request;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 시, 입력값 dto
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	/**
	 * 이메일
	 */
	@Email
	@NotBlank
	private String email;

	/**
	 * 비밀번호
	 */
	@Length(min = 8)
	private String password;
}

package com.ll.dopdang.domain.member.dto.request;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@NotBlank(message = "이메일을 입력해주세요.")
	private String email;

	/**
	 * 비밀번호
	 */
	@NotBlank(message = "비밀번호를 입력해주세요")
	@Length(min = 8, message = "비밀번호는 8자 이상 입력해야 합니다.")
	@Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "특수문자를 포함해야 합니다.")
	private String password;
}

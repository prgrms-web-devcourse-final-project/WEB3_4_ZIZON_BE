package com.ll.dopdang.domain.member.dto.request;

import com.ll.dopdang.domain.member.entity.Member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 dto
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupRequest {
	/**
	 * 이메일
	 */
	@NotBlank
	@Email
	private String email;
	/**
	 * 비밀번호
	 */
	@NotBlank
	@Size(min = 8)
	@Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*")
	private String password;
	/**
	 * 닉네임
	 */
	@NotBlank
	@Size(max = 10)
	private String name;
	/**
	 * 전화번호
	 */
	@NotBlank
	@Pattern(regexp = "^(010\\d{8}|011\\d{7})$")
	private String phone;

	/**
	 * 생성자
	 * @param member 유저
	 */
	public MemberSignupRequest(Member member) {
		this.email = member.getEmail();
		this.password = member.getPassword();
		this.name = member.getName();
		this.phone = member.getPhone();
	}
}

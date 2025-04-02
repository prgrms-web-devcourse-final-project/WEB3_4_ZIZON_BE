package com.ll.dopdang.domain.member.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 전화번호 인증 dto
 */
@Valid
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeRequest {
	/**
	 * 전화번호
	 */
	@NotBlank
	@Pattern(regexp = "^(010\\d{8}|011\\d{7})$",
		message = "전화번호는 대시없이 입력하셔야 합니다.")
	private String phone;

	private String code;
}

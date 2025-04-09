package com.ll.dopdang.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordUpdateRequest {
	@NotBlank
	@Size(min = 8)
	@Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*")
	private String currentPassword;
	@NotBlank
	@Size(min = 8)
	@Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*")
	private String newPassword;
}

package com.ll.dopdang.domain.member.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Valid
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
	@NotBlank
	@Size(max = 10)
	private String name;
	@NotBlank
	@Size(min = 8)
	@Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*")
	private String password;
}

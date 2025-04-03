package com.ll.dopdang.domain.member.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Valid
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
	@Size(max = 10)
	private String name;

	private String profileImage;
}

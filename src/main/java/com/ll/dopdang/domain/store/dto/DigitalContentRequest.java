package com.ll.dopdang.domain.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DigitalContentRequest(
	@NotBlank(message = "파일 이름은 필수입니다.")
	@Size(max = 255, message = "파일 이름은 최대 255자까지 입력 가능합니다.")
	String fileName,

	@NotBlank(message = "파일 URL은 필수입니다.")
	@Size(max = 255, message = "파일 URL은 최대 255자까지 입력 가능합니다.")
	String fileUrl,

	@NotNull(message = "파일 크기는 필수입니다.")
	@Min(value = 0, message = "파일 크기는 0 이상이어야 합니다.")
	Long fileSize,

	@NotBlank(message = "파일 타입은 필수입니다.")
	@Size(max = 50, message = "파일 타입은 최대 50자까지 입력 가능합니다.")
	String fileType,

	@Min(value = -1, message = "다운로드 제한은 -1(무제한) 이상이어야 합니다.")
	Integer downloadLimit
) {
}

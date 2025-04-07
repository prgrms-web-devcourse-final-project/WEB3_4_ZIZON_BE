package com.ll.dopdang.global.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * S3 Presigned URL 요청 DTO
 * - 업로드할 폴더, 파일 이름, MIME 타입을 포함합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequest {

	@Schema(description = "저장할 폴더 이름 (예: projects, members, products, portfolios)", example = "projects")
	@NotBlank(message = "폴더 이름은 필수입니다.")
	private String folder;

	@Schema(description = "파일 이름 (확장자 포함)", example = "image.png")
	@NotBlank(message = "파일 이름은 필수입니다.")
	private String fileName;

	@Schema(description = "파일의 MIME 타입", example = "image/png")
	@NotBlank(message = "Content-Type은 필수입니다.")
	private String contentType;
}

package com.ll.dopdang.global.s3;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
@Tag(name = "S3 Presigned URL", description = "S3 파일 업로드용 Presigned URL 생성 API")
public class S3Controller {

	private final S3Service s3Service;

	@Operation(
		summary = "S3 Presigned URL 생성",
		description = "업로드할 폴더, 파일 이름, Content-Type을 받아 Presigned URL과 접근 URL을 반환합니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 누락된 파라미터)", content = @Content),
			@ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
		}
	)
	@PostMapping("/upload-url")
	public ResponseEntity<PresignedUrlResponse> createPresignedUploadUrl(
		@Valid @RequestBody PresignedUrlRequest request
	) {
		PresignedUrlResponse response = s3Service.generatePresignedUrl(request);
		return ResponseEntity.ok(response);
	}
}

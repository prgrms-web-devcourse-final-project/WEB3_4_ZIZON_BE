package com.ll.dopdang.global.s3;

import java.net.URL;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/s3")
@Tag(name = "S3 Presigned URL", description = "S3 파일 업로드용 Presigned URL 생성 API")
@RequiredArgsConstructor
public class S3Controller {

	private final S3Service s3Service;

	@Operation(
		summary = "프로젝트 이미지 업로드용 Presigned URL 생성",
		description = "특정 프로젝트에 첨부할 이미지 업로드용 URL을 생성합니다. 프론트는 이 URL로 직접 PUT 요청을 보내면 됩니다.",
		responses = {
			@ApiResponse(responseCode = "200", description = "Presigned URL 생성 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 누락된 파라미터)"),
			@ApiResponse(responseCode = "500", description = "서버 내부 오류 (Presigned URL 생성 실패 등)")
		}
	)
	@GetMapping("/project-image")
	public ResponseEntity<PresignedUrlResponse> getProjectImagePresignedUrl(
		@Parameter(description = "프로젝트 ID", example = "1") @RequestParam Long projectId,
		@Parameter(description = "저장할 파일 이름 (확장자 포함)",
			example = "project_image.png") @RequestParam String fileName,
		@Parameter(description = "파일 MIME 타입 (예: image/png, image/jpeg)",
			example = "image/png") @RequestParam String contentType
	) {
		URL url = s3Service.generatePresignedUrlForProjectImage(projectId, fileName, contentType);
		return ResponseEntity.ok(new PresignedUrlResponse(url.toString()));
	}

	@Operation(
		summary = "회원 프로필 이미지 업로드용 Presigned URL 생성",
		description = "회원의 프로필 이미지를 업로드할 수 있는 Presigned URL을 생성합니다."
	)
	@GetMapping("/member-profile")
	public ResponseEntity<PresignedUrlResponse> getMemberProfilePresignedUrl(
		@Parameter(description = "회원 ID", example = "42") @RequestParam Long memberId,
		@Parameter(description = "저장할 파일 이름 (확장자 포함)",
			example = "profile_image.png") @RequestParam String fileName,
		@Parameter(description = "파일 MIME 타입 (예: image/png, image/jpeg)",
			example = "image/png") @RequestParam String contentType
	) {
		URL url = s3Service.generatePresignedUrlForMemberProfile(memberId, fileName, contentType);
		return ResponseEntity.ok(new PresignedUrlResponse(url.toString()));
	}
}

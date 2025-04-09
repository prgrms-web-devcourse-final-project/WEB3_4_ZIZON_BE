package com.ll.dopdang.domain.chatroom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.global.s3.PresignedUrlRequest;
import com.ll.dopdang.global.s3.PresignedUrlResponse;
import com.ll.dopdang.global.s3.S3Service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatUploadController {

	private final S3Service s3Service;

	/**
	 * S3에 이미지를 업로드하기 위해 프리사인 URL을 발급합니다.
	 * 클라이언트는 이 URL로 이미지를 직접 업로드한 후, 그 URL(예, accessUrl)을 채팅 메시지에 포함시켜서 전송할 수 있습니다.
	 *
	 * @param request - PresignedUrlRequest 객체: folder, fileName, contentType 정보 포함
	 * @return PresignedUrlResponse - presignedUrl과 accessUrl 정보를 포함한 응답
	 */
	@PostMapping("/presigned")
	public ResponseEntity<PresignedUrlResponse> createPresignedUrl(
		@Valid @RequestBody PresignedUrlRequest request
	) {
		// folder: 예를 들어 "chat" 또는 "chatImages" 등으로 지정
		// fileName: 클라이언트가 업로드할 파일 이름 (예: "image.png")
		// contentType: MIME 타입 (예: "image/png")
		// S3Service의 generatePresignedUrl() 메서드를 호출하여 프리사인 URL을 생성합니다.
		PresignedUrlResponse response = s3Service.generatePresignedUrl(request);
		return ResponseEntity.ok(response);
	}
}

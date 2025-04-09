package com.ll.dopdang.domain.review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.review.dto.ReviewCreateRequest;
import com.ll.dopdang.domain.review.dto.ReviewCreateResponse;
import com.ll.dopdang.domain.review.sevice.ReviewService;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Review", description = "리뷰 관련 API")
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ReviewService reviewService;

	/**
	 * 리뷰를 생성합니다.
	 *
	 * @param request 리뷰 생성 요청 DTO
	 * @param userDetails 현재 로그인한 사용자 정보
	 * @return 생성된 리뷰 정보 응답 DTO
	 */
	@Operation(summary = "리뷰 생성", description = "계약이 완료된 프로젝트에 대해 리뷰를 작성합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "리뷰 생성 성공"),
		@ApiResponse(responseCode = "400", description = "유효하지 않은 요청 데이터"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
		@ApiResponse(responseCode = "404", description = "계약을 찾을 수 없음 또는 이미 리뷰가 존재함")
	})
	@PostMapping
	public ResponseEntity<ReviewCreateResponse> createReview(
		@Valid @RequestBody ReviewCreateRequest request,
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
	) {
		if (userDetails == null) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}

		Long reviewerId = userDetails.getId();
		ReviewCreateResponse response = reviewService.createReview(request.getProjectId(), reviewerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}

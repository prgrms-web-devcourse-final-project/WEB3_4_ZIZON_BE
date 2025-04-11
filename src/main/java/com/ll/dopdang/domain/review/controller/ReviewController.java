package com.ll.dopdang.domain.review.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.review.dto.ClientReviewPageResponse;
import com.ll.dopdang.domain.review.dto.ExpertReviewPageResponse;
import com.ll.dopdang.domain.review.dto.ReviewCreateRequest;
import com.ll.dopdang.domain.review.dto.ReviewCreateResponse;
import com.ll.dopdang.domain.review.dto.ReviewDetailResponse;
import com.ll.dopdang.domain.review.dto.ReviewStatsResponse;
import com.ll.dopdang.domain.review.sevice.ReviewService;
import com.ll.dopdang.domain.review.sevice.ReviewStatsService;
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
	private final ReviewStatsService reviewStatsService;

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

	@Operation(
		summary = "프로젝트 기반 리뷰 단건 조회",
		description = "프로젝트 ID를 통해 리뷰 상세 정보를 조회합니다."
	)
	@ApiResponse(responseCode = "200", description = "리뷰 조회 성공")
	@ApiResponse(responseCode = "404", description = "리뷰 또는 계약을 찾을 수 없음")
	@GetMapping("/project/{projectId}")
	public ResponseEntity<ReviewDetailResponse> getReviewByProjectId(
		@PathVariable Long projectId
	) {
		ReviewDetailResponse response = reviewService.getReviewByProjectId(projectId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 전문가가 받은 리뷰 목록 조회 API
	 * 비회원 포함 누구나 접근 가능하며,
	 * 계약이 완료(COMPLETED)된 리뷰만 조회됨.
	 * 최신 작성일 순으로 정렬되며, 무한스크롤 대응.
	 *
	 * @param expertId 조회 대상 전문가 ID
	 * @param pageable 페이지 정보 (기본 size: 10, 정렬: createdAt DESC)
	 * @return 리뷰 페이지 응답
	 */
	@Operation(
		summary = "전문가가 받은 리뷰 목록 조회",
		description = "특정 전문가가 받은 리뷰 목록을 최신순으로 반환합니다. 무한스크롤 방식 지원.",
		tags = {"Review"}
	)
	@GetMapping("/experts/{expertId}")
	public ResponseEntity<ExpertReviewPageResponse> getExpertReviews(
		@PathVariable Long expertId,
		@ParameterObject
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
		Pageable pageable
	) {
		ExpertReviewPageResponse response = reviewService.getReviewsByExpert(expertId, pageable);
		return ResponseEntity.ok(response);
	}

	/**
	 * 클라이언트가 작성한 리뷰 목록 조회 API
	 * 마이페이지에서 내가 작성한 리뷰들을 조회할 수 있습니다.
	 * 작성일 기준 최신순으로 정렬되며, 무한스크롤 방식 지원.
	 * @param pageable 페이징 정보 (기본 10개씩, 최신순 정렬)
	 * @param userDetails 로그인한 사용자 정보
	 * @return 리뷰 목록 + 페이지 정보
	 */
	@GetMapping("/clients/me")
	@Operation(
		summary = "내가 작성한 리뷰 목록 조회 (마이페이지)",
		description = "로그인한 사용자가 작성한 리뷰 목록을 최신순으로 조회합니다. 무한스크롤 방식 지원.",
		tags = {"Review"}
	)
	@ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
	@ApiResponse(responseCode = "404", description = "리뷰 또는 계약을 찾을 수 없음")
	@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	public ResponseEntity<ClientReviewPageResponse> getMyReviews(
		@ParameterObject
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
	) {
		if (userDetails == null) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}

		Long clientId = userDetails.getId();
		ClientReviewPageResponse response = reviewService.getClientReviews(clientId, pageable);
		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "전문가 리뷰 통계 조회",
		description = "전문가의 평균 평점과 총 리뷰 수를 반환합니다."
	)
	@GetMapping("/experts/{expertId}/review-stats")
	public ResponseEntity<ReviewStatsResponse> getExpertReviewStats(
		@PathVariable Long expertId
	) {
		ReviewStatsResponse response = reviewStatsService.getStatsByExpertId(expertId);
		return ResponseEntity.ok(response);
	}
}

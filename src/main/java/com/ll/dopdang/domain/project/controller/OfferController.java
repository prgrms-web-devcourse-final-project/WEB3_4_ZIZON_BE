package com.ll.dopdang.domain.project.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.project.dto.OfferCreateRequest;
import com.ll.dopdang.domain.project.dto.OfferDetailResponse;
import com.ll.dopdang.domain.project.entity.Offer;
import com.ll.dopdang.domain.project.service.OfferService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class OfferController {
	private final OfferService offerService;

	/**
	 * 제안서 작성 API
	 * @param customUserDetails 인증된 유저 정보
	 * @param projectId 프로젝트 고유 ID
	 * @param request 제안서 작성 dto
	 * @return {@link ResponseEntity}
	 */
	@Operation(
		summary = "제안서 작성",
		description = "프로젝트 ID를 기반으로 제안서를 작성합니다.",
		tags = {"Project"}
	)
	@ApiResponse(responseCode = "201", description = "제안서 작성 성공")
	@ApiResponse(responseCode = "403", description = "전문가가 아님")
	@ApiResponse(responseCode = "404", description = "프로젝트를 못 찾음")
	@PostMapping("/{project_id}/offers")
	public ResponseEntity<Map<String, String>> createOffer(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable("project_id") Long projectId,
		@Valid @RequestBody OfferCreateRequest request
	) {
		offerService.createOffer(customUserDetails, projectId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "제안서를 작성하였습니다."));
	}

	/**
	 * 제안서 단건 조회 API
	 * @param projectId 프로젝트 고유 ID
	 * @param offerId 제안서 고유 ID
	 * @param customUserDetails 인증된 유저 정보
	 * @return {@link ResponseEntity<OfferDetailResponse>}
	 */
	@Operation(summary = "제안서 단건 조회", description = "프로젝트와 제안서 ID를 기반으로 ")
	@GetMapping("/{project_id}/offers/{offer_id}")
	public ResponseEntity<OfferDetailResponse> getOfferById(
		@PathVariable("project_id") Long projectId,
		@PathVariable("offer_id") Long offerId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		OfferDetailResponse response = offerService.getOfferById(customUserDetails, projectId, offerId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{projectId}/offers/expert/{expertId}")
	public ResponseEntity<OfferDetailResponse> getOfferDetail(
		@PathVariable Long projectId,
		@PathVariable Long expertId,
		@AuthenticationPrincipal CustomUserDetails customUserDetails
	) {
		Offer offer = offerService.getOfferByProjectAndExpert(projectId, expertId, customUserDetails);
		return ResponseEntity.ok(OfferDetailResponse.from(offer));
	}
}

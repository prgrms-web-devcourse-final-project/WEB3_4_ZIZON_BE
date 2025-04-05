package com.ll.dopdang.domain.project.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.project.dto.ContractCreateRequest;
import com.ll.dopdang.domain.project.dto.ContractDetailResponse;
import com.ll.dopdang.domain.project.dto.ContractSummaryResponse;
import com.ll.dopdang.domain.project.service.ContractService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

	private final ContractService contractService;

	/**
	 * 계약 생성 API
	 * @param request 계약 생성 요청 정보
	 * @return 생성된 계약 ID
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> createContract(@RequestBody ContractCreateRequest request) {
		log.info("계약 생성 요청: offerId={}, price={}, startDate={}, endDate={}",
			request.getOfferId(), request.getPrice(), request.getStartDate(), request.getEndDate());

		Long contractId = contractService.createContractFromOffer(
			request.getOfferId(),
			request.getPrice(),
			request.getStartDate(),
			request.getEndDate()
		);

		Map<String, Object> response = new HashMap<>();
		response.put("contractId", contractId);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "전문가 계약 목록 조회", description = "로그인한 전문가의 계약 목록을 조회합니다. (무한스크롤 오프셋 기반)")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패")
	@ApiResponse(responseCode = "403", description = "전문가만 조회 가능")
	@GetMapping("/my")
	public ResponseEntity<List<ContractSummaryResponse>> getMyContracts(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Member member = userDetails.getMember();

		if (member.isClient() || !"EXPERT".equalsIgnoreCase(member.getUserRole())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 전문가가 아닐 경우
		}

		List<ContractSummaryResponse> contracts = contractService.getContractsForExpert(member.getId(), pageable);
		return ResponseEntity.ok(contracts);
	}

	@GetMapping("/by-project/{projectId}")
	@Operation(summary = "계약 단건 조회", description = "프로젝트 ID로 계약을 단건 조회합니다. (클라이언트 또는 전문가 본인만 접근 가능)")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "403", description = "접근 권한 없음")
	@ApiResponse(responseCode = "404", description = "계약 정보 없음")
	public ResponseEntity<ContractDetailResponse> getContractByProjectId(
		@PathVariable Long projectId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		Member member = userDetails.getMember();
		ContractDetailResponse response = contractService.getContractDetail(projectId, member);
		return ResponseEntity.ok(response);
	}
}

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
import org.springframework.web.bind.annotation.PatchMapping;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Contract", description = "계약 관련 API")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

	private final ContractService contractService;

	/**
	 * 계약 생성 API
	 * 선택된 오퍼에 대해 계약을 생성하고, 해당 오퍼는 ACCEPTED, 나머지는 REJECTED 처리됩니다.
	 *
	 * @param request 계약 생성 요청 정보
	 * @return 생성된 계약 ID
	 */
	@Operation(
		summary = "계약 생성",
		description = "선택된 오퍼를 기반으로 계약을 생성합니다. 계약 생성 시 해당 오퍼는 ACCEPTED 상태로 변경되고, 같은 프로젝트의 다른 오퍼는 모두 REJECTED 됩니다."
	)

	@ApiResponse(responseCode = "200", description = "계약 생성 성공")
	@ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 유효성 검증 실패")
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

	/**
	 * 전문가 마이페이지 - 계약 목록 조회 (무한 스크롤)
	 *
	 * @param userDetails 로그인한 전문가 정보
	 * @param pageable 페이징 정보 (오프셋 기반)
	 * @return 전문가가 참여한 계약 목록
	 */
	@Operation(
		summary = "전문가 계약 목록 조회",
		description = "로그인한 전문가의 계약 목록을 조회합니다. 오프셋 기반 무한 스크롤 방식입니다."
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패")
	@ApiResponse(responseCode = "403", description = "전문가만 접근 가능")
	@GetMapping("/my")
	public ResponseEntity<List<ContractSummaryResponse>> getMyContracts(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Member member = userDetails.getMember();

		// 전문가가 아닌 경우 접근 제한
		if (member.isClient() || !"EXPERT".equalsIgnoreCase(member.getUserRole())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		List<ContractSummaryResponse> contracts = contractService.getContractsForExpert(member.getId(), pageable);
		return ResponseEntity.ok(contracts);
	}

	/**
	 * 계약 단건 조회 (프로젝트 ID 기준)
	 * 로그인한 사용자가 클라이언트이거나 전문가 본인인 경우만 접근 허용
	 *
	 * @param projectId 조회할 계약이 속한 프로젝트 ID
	 * @param userDetails 인증된 사용자 정보
	 * @return 계약 상세 정보
	 */
	@Operation(
		summary = "계약 단건 조회 (프로젝트 기준)",
		description = "프로젝트 ID로 연결된 계약 정보를 조회합니다. 계약 당사자 (클라이언트 또는 전문가)만 접근 가능합니다."
	)

	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "403", description = "접근 권한 없음")
	@ApiResponse(responseCode = "404", description = "해당 프로젝트에 연결된 계약이 없음")
	@GetMapping("/by-project/{projectId}")
	public ResponseEntity<ContractDetailResponse> getContractByProjectId(
		@PathVariable Long projectId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Member member = userDetails.getMember();
		ContractDetailResponse response = contractService.getContractDetail(projectId, member);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "계약 완료 처리", description = "클라이언트가 자신의 프로젝트에 해당하는 계약을 완료 상태로 변경합니다.")
	@ApiResponse(responseCode = "204", description = "계약 완료 처리 성공")
	@ApiResponse(responseCode = "404", description = "계약을 찾을 수 없음")
	@ApiResponse(responseCode = "401", description = "접근 권한이 없음")
	@PatchMapping("/{contractId}/complete")
	public ResponseEntity<Void> completeContract(
		@Parameter(description = "계약 ID", required = true) @PathVariable Long contractId,
		@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
	) {
		contractService.updateContractStatusToAsCompleted(contractId, userDetails.getId());
		return ResponseEntity.noContent().build();
	}
}

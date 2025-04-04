package com.ll.dopdang.domain.project.controller;

import java.util.HashMap;
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

import com.ll.dopdang.domain.project.dto.ContractCreateRequest;
import com.ll.dopdang.domain.project.service.ContractService;
import com.ll.dopdang.domain.project.dto.ProjectCreateRequest;
import com.ll.dopdang.domain.project.dto.ProjectCreateResponse;
import com.ll.dopdang.domain.project.dto.ProjectDetailResponse;
import com.ll.dopdang.domain.project.service.ProjectService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

	private final ProjectService projectService;
	private final ContractService contractService;

	@Operation(
		summary = "프로젝트 생성",
		description = "고객이 새로운 프로젝트를 생성합니다. 인증된 사용자의 ID를 기반으로 프로젝트가 생성되며, "
			+ "요청 본문에는 프로젝트의 카테고리, 제목, 지역, 예산, 마감일 등이 포함되어야 합니다.",
		tags = {"Project"}
	)
	@ApiResponse(responseCode = "201", description = "프로젝트 생성 성공")
	@ApiResponse(responseCode = "400", description = "유효하지 않은 입력 값")
	@ApiResponse(responseCode = "401", description = "인증 실패")
	@ApiResponse(responseCode = "404", description = "카테고리 또는 전문가를 찾을 수 없음")
	@PostMapping
	public ResponseEntity<ProjectCreateResponse> createProject(
		@io.swagger.v3.oas.annotations.parameters.RequestBody(
			description = "프로젝트 생성 요청 객체", required = true
		)
		@Valid @RequestBody ProjectCreateRequest request,

		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Long clientId = userDetails.getId();
		Long projectId = projectService.createProject(request, clientId);

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(new ProjectCreateResponse(
				projectId,
				"프로젝트가 성공적으로 생성되었습니다. (ID: " + projectId + ")"
			));
	}

	@PostMapping("/contract")
	public ResponseEntity<?> createContract(@RequestBody ContractCreateRequest request) {
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

	@Operation(
		summary = "프로젝트 단건 조회",
		description = "프로젝트 ID를 기반으로 단일 프로젝트의 상세 정보를 조회합니다.",
		tags = {"Project"}
	)
	@ApiResponse(responseCode = "200", description = "프로젝트 조회 성공")
	@ApiResponse(responseCode = "404", description = "해당 프로젝트를 찾을 수 없음")
	@GetMapping("/{projectId}")
	public ResponseEntity<ProjectDetailResponse> getProjectById(
		@Parameter(description = "조회할 프로젝트의 ID", required = true, example = "1")
		@PathVariable Long projectId
	) {
		ProjectDetailResponse response = projectService.getProjectById(projectId);
		return ResponseEntity.ok(response);
	}
}

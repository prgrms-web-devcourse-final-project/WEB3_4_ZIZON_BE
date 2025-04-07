package com.ll.dopdang.domain.expert.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.request.ExpertUpdateRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertDetailResponseDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.service.ExpertService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/experts")
@RequiredArgsConstructor
@Tag(name = "전문가", description = "전문가 프로필 관련 API")
public class ExpertController {

	private final ExpertService expertService;
	private static final String MESSAGE = "message";

	@Operation(summary = "전문가 프로필 등록", description = "회원이 전문가 프로필을 등록합니다.")
	@ApiResponse(responseCode = "200", description = "전문가 등록 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패")
	@ApiResponse(responseCode = "400", description = "잘못된 요청")
	@PostMapping

	public ResponseEntity<Map<Long, String>> createExpert(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@RequestBody ExpertRequestDto requestDto) throws Exception {
		Long memberId = customUserDetails.getId();
		Long expertId = expertService.createExpert(requestDto, memberId);

		Map<Long, String> response = new HashMap<>();
		response.put(expertId, "전문가 프로필을 등록하였습니다");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "전문가 목록 조회", description = "모든 전문가 목록을 조회합니다. 카테고리명 또는 경력 수준으로 필터링할 수 있습니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public ResponseEntity<List<ExpertResponseDto>> getExperts(
		@Parameter(description = "필터링할 카테고리 이름 목록", example = "개발,디자인")
		@RequestParam(required = false) List<String> categoryNames,

		@Parameter(description = "필터링할 경력 수준", example = "SENIOR")
		@RequestParam(required = false) String careerLevel
	) {
		return ResponseEntity.ok(expertService.getAllExperts(categoryNames, careerLevel));
	}

	@Operation(summary = "전문가 상세 조회", description = "전문가 ID를 통해 해당 전문가의 상세 정보를 조회합니다.")

	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 전문가 ID")
	@GetMapping("/{expertId}")
	public ResponseEntity<ExpertDetailResponseDto> getExpertById(
		@Parameter(description = "전문가 ID", example = "1")
		@PathVariable Long expertId
	) {
		return ResponseEntity.ok(expertService.getExpertById(expertId));
	}

	@Operation(summary = "전문가 프로필 수정", description = "전문가 ID를 통해 해당 전문가의 프로필을 수정합니다.")
	@ApiResponse(responseCode = "200", description = "수정 성공")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 전문가 ID")
	@PutMapping("/{expertId}")
	public ResponseEntity<Map<String, String>> updateExpert(
		@Parameter(description = "전문가 ID", example = "1")
		@PathVariable Long expertId,

		@RequestBody ExpertUpdateRequestDto updateRequestDto
	) {
		expertService.updateExpert(expertId, updateRequestDto);

		Map<String, String> response = new HashMap<>();
		response.put(MESSAGE, "전문가 프로필을 수정하였습니다");
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "전문가 삭제", description = "전문가 ID를 통해 전문가 프로필을 삭제합니다.")

	@ApiResponse(responseCode = "200", description = "삭제 성공")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 전문가 ID")
	@DeleteMapping("/{expertId}")
	public ResponseEntity<Map<String, String>> deleteExpert(
		@Parameter(description = "전문가 ID", example = "1")
		@PathVariable Long expertId
	) {
		expertService.deleteExpert(expertId);

		Map<String, String> response = new HashMap<>();
		response.put(MESSAGE, "전문가 프로필을 삭제하였습니다");
		return ResponseEntity.ok(response);
	}
}

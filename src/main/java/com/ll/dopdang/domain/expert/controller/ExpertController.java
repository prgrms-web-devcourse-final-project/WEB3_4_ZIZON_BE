package com.ll.dopdang.domain.expert.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.request.ExpertUpdateRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertDetailResponseDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.service.ExpertService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/experts")
@RequiredArgsConstructor
public class ExpertController {

	private final ExpertService expertService;

	@PostMapping
	public ResponseEntity<Map<String, String>> createExpert(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@RequestBody ExpertRequestDto requestDto) throws Exception {
		Long memberId = customUserDetails.getId();
		expertService.createExpert(requestDto, memberId);
		Map<String, String> response = new HashMap<>();
		response.put("message", "전문가 프로필을 등록하였습니다");
		return new ResponseEntity<>(response, HttpStatus.valueOf(200));
	}

	@GetMapping
	public ResponseEntity<List<ExpertResponseDto>> getExperts(
		@RequestParam(required = false) List<String> categoryNames,
		@RequestParam(required = false) String careerLevel
	) {
		return new ResponseEntity<>(expertService.getAllExperts(categoryNames, careerLevel), HttpStatus.valueOf(200));
	}

	@GetMapping("/{expertId}")
	public ResponseEntity<ExpertDetailResponseDto> getExpertById(@PathVariable Long expertId) {
		return new ResponseEntity<>(expertService.getExpertById(expertId), HttpStatus.valueOf(200));
	}

	@PutMapping("/{expertId}")
	public ResponseEntity<Map<String, String>> updateExpert(
		@PathVariable Long expertId,
		@RequestBody ExpertUpdateRequestDto updateRequestDto) {
		expertService.updateExpert(expertId, updateRequestDto);
		Map<String, String> response = new HashMap<>();
		response.put("message", "전문가 프로필을 수정하였습니다");
		return new ResponseEntity<>(response, HttpStatus.valueOf(200));
	}

	/**
	 * 전문가 삭제 API
	 *
	 * @param expertId 삭제하려는 전문가 ID
	 * @return 삭제 성공 여부 응답
	 */
	@DeleteMapping("/{expertId}")
	public ResponseEntity<Map<String, String>> deleteExpert(@PathVariable Long expertId) {
		expertService.deleteExpert(expertId);
		Map<String, String> response = new HashMap<>();
		response.put("message", "전문가 프로필을 삭제정하였습니다");
		return new ResponseEntity<>(response, HttpStatus.valueOf(200));

	}
}

package com.ll.dopdang.domain.payment.controller;

import java.time.YearMonth;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.payment.service.RebateService;
import com.ll.dopdang.domain.payment.util.RebateVerifier;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 관리자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/admin/rebate")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "정산 관리자 API", description = "정산 데이터 검증 및 관리 API")
public class RebateAdminController {

	private final RebateVerifier rebateVerifier;
	private final RebateService rebateService;

	@Operation(summary = "월별 정산 데이터 검증", description = "특정 월의 정산 데이터를 검증합니다.")
	@GetMapping("/verify")
	public ResponseEntity<Map<String, Object>> verifyRebate(
		@Parameter(description = "정산 대상 년월 (yyyy-MM 형식, 미입력시 전월)", example = "2025-04")
		@RequestParam(defaultValue = "#{T(java.time.YearMonth).now().minusMonths(1).toString()}")
		@DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

		Map<String, Object> verificationResult = rebateVerifier.verifyMonthlyRebate(yearMonth);
		return ResponseEntity.ok(verificationResult);
	}

	@Operation(summary = "전문가별 정산 데이터 검증", description = "특정 전문가의 정산 데이터를 검증합니다.")
	@GetMapping("/verify/expert/{expertId}")
	public ResponseEntity<Map<String, Object>> verifyExpertRebate(
		@PathVariable Long expertId,
		@Parameter(description = "정산 대상 년월 (yyyy-MM 형식, 미입력시 전월)", example = "2025-04")
		@RequestParam(defaultValue = "#{T(java.time.YearMonth).now().minusMonths(1).toString()}")
		@DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

		Map<String, Object> verificationResult = rebateVerifier.verifyExpertRebate(expertId, yearMonth);
		return ResponseEntity.ok(verificationResult);
	}

	@Operation(summary = "수동 정산 데이터 생성", description = "특정 월의 정산 데이터를 수동으로 생성합니다.")
	@GetMapping("/create")
	public ResponseEntity<Map<String, Object>> createRebateData(
		@Parameter(description = "정산 대상 년월 (yyyy-MM 형식, 미입력시 전월)", example = "2025-04")
		@RequestParam(defaultValue = "#{T(java.time.YearMonth).now().minusMonths(1).toString()}")
		@DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

		int count = rebateService.createRebateDataManually(yearMonth);

		Map<String, Object> result = Map.of(
			"yearMonth", yearMonth.toString(),
			"count", count,
			"message", count + "개의 정산 데이터가 생성되었습니다."
		);

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "정산 처리 실행", description = "정산 처리를 수동으로 실행합니다. 년월을 지정하지 않으면 전월 정산을 처리합니다.")
	@GetMapping("/process")
	public ResponseEntity<Map<String, Object>> processRebate(
		@Parameter(description = "정산 대상 년월 (yyyy-MM 형식, 미입력시 전월)", example = "2023-05")
		@RequestParam(defaultValue = "#{T(java.time.YearMonth).now().minusMonths(1).toString()}")
		@DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

		// 서비스 메소드 호출
		rebateService.processMonthlyRebate(yearMonth);

		String message = String.format("%s에 대한 정산 처리가 완료되었습니다.", yearMonth);

		Map<String, Object> result = Map.of("message", message);

		return ResponseEntity.ok(result);
	}
}

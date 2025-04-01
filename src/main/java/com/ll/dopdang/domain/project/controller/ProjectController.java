package com.ll.dopdang.domain.project.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.project.dto.ContractCreateRequest;
import com.ll.dopdang.domain.project.service.ContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/project")
public class ProjectController {

	private final ContractService contractService;

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
}

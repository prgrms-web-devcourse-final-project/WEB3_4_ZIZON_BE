package com.ll.dopdang.domain.payment.strategy.info;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.entity.Project;
import com.ll.dopdang.domain.project.service.ContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로젝트 결제 정보 제공 전략 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectPaymentInfoProvider implements PaymentOrderInfoProvider {

	private final ContractService contractService;

	@Override
	public Map<String, Object> provideAdditionalInfo(Long referenceId) {
		log.debug("프로젝트 결제 정보 조회: referenceId={}", referenceId);

		Contract contract = contractService.getContractById(referenceId);
		Project project = contract.getProject();

		Map<String, Object> additionalInfo = new HashMap<>();

		// 계약 정보 추가
		additionalInfo.put("expertName", contract.getExpert().getName());
		additionalInfo.put("category", project.getCategory().getName());
		additionalInfo.put("price", contract.getPrice());
		additionalInfo.put("startDate", contract.getStartDate());
		additionalInfo.put("endDate", contract.getEndDate());

		return additionalInfo;
	}
}

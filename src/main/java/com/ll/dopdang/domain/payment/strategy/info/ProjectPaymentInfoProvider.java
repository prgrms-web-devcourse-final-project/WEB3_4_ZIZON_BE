package com.ll.dopdang.domain.payment.strategy.info;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
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
		additionalInfo.put("expertName", contract.getExpert().getMember().getName());
		additionalInfo.put("title", project.getTitle());
		additionalInfo.put("price", contract.getPrice());
		additionalInfo.put("categoryName", project.getCategory().getName());
		additionalInfo.put("startDate", contract.getStartDate());
		additionalInfo.put("endDate", contract.getEndDate());
		additionalInfo.put("categoryId", project.getCategory().getId());
		additionalInfo.put("projectId", project.getId());
		additionalInfo.put("contractId", contract.getId());
		additionalInfo.put("expertId", contract.getExpert().getId());
		additionalInfo.put("clientId", contract.getClient().getId());

		return additionalInfo;
	}

	@Override
	public PaymentResultResponse enrichPaymentResult(Payment payment, PaymentResultResponse baseResponse) {

		// 결제 정보에서 참조 ID로 계약 정보 조회
		Contract contract = contractService.getContractById(payment.getReferenceId());
		// 기존 응답에 전문가 이름 추가
		return PaymentResultResponse.builder()
			.status(baseResponse.getStatus())
			.amount(baseResponse.getAmount())
			.errorCode(baseResponse.getErrorCode())
			.message(baseResponse.getMessage())
			.expertName(contract.getExpert().getMember().getName())
			.paymentName(payment.getItemsSummary())
			.build();
	}
}

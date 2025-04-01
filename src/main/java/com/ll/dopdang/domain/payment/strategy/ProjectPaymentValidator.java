package com.ll.dopdang.domain.payment.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.service.ContractService;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프로젝트 결제 금액 검증 전략 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectPaymentValidator implements PaymentAmountValidator {

	private final ContractService contractService;

	@Override
	public BigDecimal validateAndGetExpectedAmount(Long referenceId, BigDecimal requestAmount) {
		Contract contract = contractService.getContractById(referenceId);
		BigDecimal expectedAmount = contract.getPrice();

		if (expectedAmount.compareTo(requestAmount) != 0) {
			log.error("결제 금액 불일치: 예상 금액={}, 요청 금액={}", expectedAmount, requestAmount);
			throw new ServiceException(
				ErrorCode.PAYMENT_AMOUNT_MISMATCH,
				String.format("결제 금액이 일치하지 않습니다. 예상 금액: %s, 결제 요청 금액: %s",
					expectedAmount, requestAmount)
			);
		}

		log.debug("결제 금액 검증 성공: 예상 금액={}, 요청 금액={}", expectedAmount, requestAmount);
		return expectedAmount;
	}
}

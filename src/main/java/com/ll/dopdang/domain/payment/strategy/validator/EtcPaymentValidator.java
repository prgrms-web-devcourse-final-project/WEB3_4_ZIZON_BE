package com.ll.dopdang.domain.payment.strategy.validator;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 기타 결제 금액 검증 전략 구현
 */
@Slf4j
@Component
public class EtcPaymentValidator implements PaymentAmountValidator {

	@Override
	public BigDecimal validateAndGetExpectedAmount(Long referenceId, BigDecimal requestAmount) {
		// Todo: 기타 결제 유형에 대한 금액 검증 로직 추가 필요

		// 임시 처리: 요청 금액을 그대로 반환
		log.debug("기타 결제 금액 검증 (임시 처리): 요청 금액={}", requestAmount);
		return requestAmount;
	}
}

package com.ll.dopdang.domain.payment.strategy.validator;

import java.math.BigDecimal;

/**
 * 결제 금액 검증을 위한 전략 인터페이스
 */
public interface PaymentAmountValidator {

	/**
	 * 결제 금액이 유효한지 검증하고 예상 금액을 반환합니다.
	 *
	 * @param referenceId 참조 ID
	 * @param requestAmount 결제 요청 금액
	 * @return 예상 금액
	 */
	BigDecimal validateAndGetExpectedAmount(Long referenceId, BigDecimal requestAmount);
}

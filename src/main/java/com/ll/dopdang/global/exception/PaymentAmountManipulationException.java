package com.ll.dopdang.global.exception;

import java.math.BigDecimal;

import lombok.Getter;

/**
 * 결제 금액 조작이 감지되었을 때 발생하는 예외
 */
@Getter
public class PaymentAmountManipulationException extends ServiceException {

	private final Long referenceId;
	private final BigDecimal expectedAmount;
	private final BigDecimal requestAmount;

	/**
	 * 결제 금액 조작 예외 생성자
	 *
	 * @param referenceId 참조 ID
	 * @param expectedAmount 예상 금액
	 * @param requestAmount 요청 금액
	 */
	public PaymentAmountManipulationException(Long referenceId, BigDecimal expectedAmount, BigDecimal requestAmount) {
		super(
			ErrorCode.PAYMENT_AMOUNT_MISMATCH,
			String.format("결제 금액이 일치하지 않습니다. 예상 금액: %s, 결제 요청 금액: %s", expectedAmount, requestAmount)
		);
		this.referenceId = referenceId;
		this.expectedAmount = expectedAmount;
		this.requestAmount = requestAmount;
	}
}

package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;

import com.ll.dopdang.domain.payment.entity.Payment;

/**
 * 결제 정보 저장 전략 인터페이스
 */
public interface PaymentSaver {

	/**
	 * 결제 정보를 저장합니다.
	 *
	 * @param referenceId 참조 ID
	 * @param amount 결제 금액
	 * @param fee 수수료
	 * @param paymentKey 결제 키
	 * @return 저장된 Payment 엔티티
	 */
	Payment savePayment(Long referenceId, BigDecimal amount, BigDecimal fee, String paymentKey);
}

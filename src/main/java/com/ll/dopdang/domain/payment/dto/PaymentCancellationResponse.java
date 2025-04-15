package com.ll.dopdang.domain.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;

/**
 * 결제 취소 응답 정보를 담는 DTO
 */
public record PaymentCancellationResponse(
	Long paymentId,
	String paymentKey,
	PaymentStatus status,
	BigDecimal canceledAmount,
	BigDecimal remainingAmount,
	String message,
	LocalDateTime canceledAt) {

	/**
	 * Payment 엔티티로부터 응답 DTO를 생성합니다.
	 *
	 * @param payment 결제 정보
	 * @return 결제 취소 응답 DTO
	 */
	public static PaymentCancellationResponse from(Payment payment) {
		return new PaymentCancellationResponse(
			payment.getId(),
			payment.getPaymentKey(),
			payment.getStatus(),
			payment.getCanceledAmount(),
			payment.getRemainingAmount(),
			"결제가 성공적으로 취소되었습니다.",
			LocalDateTime.now()
		);
	}
}

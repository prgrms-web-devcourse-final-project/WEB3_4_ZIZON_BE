package com.ll.dopdang.domain.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 취소 응답 정보를 담는 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancellationResponse {

	private Long paymentId;
	private String paymentKey;
	private PaymentStatus status;
	private BigDecimal canceledAmount;
	private BigDecimal remainingAmount;
	private String message;
	private LocalDateTime canceledAt;

	/**
	 * Payment 엔티티로부터 응답 DTO를 생성합니다.
	 *
	 * @param payment 결제 정보
	 * @return 결제 취소 응답 DTO
	 */
	public static PaymentCancellationResponse from(Payment payment) {
		return PaymentCancellationResponse.builder()
			.paymentId(payment.getId())
			.paymentKey(payment.getPaymentKey())
			.status(payment.getStatus())
			.canceledAmount(payment.getCanceledAmount())
			.remainingAmount(payment.getRemainingAmount())
			.message("결제가 성공적으로 취소되었습니다.")
			.canceledAt(LocalDateTime.now())
			.build();
	}
}

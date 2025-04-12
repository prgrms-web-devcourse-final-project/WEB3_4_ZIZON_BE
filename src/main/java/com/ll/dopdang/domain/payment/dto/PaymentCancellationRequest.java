package com.ll.dopdang.domain.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 취소 요청을 처리하기 위한 DTO
 * cancelAmount가 null이면 전액 취소, 값이 있으면 부분 취소로 처리
 */
public record PaymentCancellationRequest(
	@NotNull(message = "주문 번호는 필수입니다.")
	String orderId,

	@NotNull(message = "취소 사유는 필수입니다.")
	String cancelReason,

	@DecimalMin(value = "0.01", message = "취소 금액은 0보다 커야 합니다.")
	BigDecimal cancelAmount // null이면 전액 취소
) {
}

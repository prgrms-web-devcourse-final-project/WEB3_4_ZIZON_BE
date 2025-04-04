package com.ll.dopdang.domain.payment.dto;

import java.math.BigDecimal;

import com.ll.dopdang.domain.payment.entity.PaymentType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 취소 요청을 처리하기 위한 DTO
 * cancelAmount가 null이면 전액 취소, 값이 있으면 부분 취소로 처리
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancellationRequest {

	@NotNull(message = "결제 유형은 필수입니다.")
	private PaymentType paymentType;

	@NotNull(message = "참조 ID는 필수입니다.")
	private Long referenceId;

	@NotNull(message = "취소 사유는 필수입니다.")
	private String cancelReason;

	@DecimalMin(value = "0.01", message = "취소 금액은 0보다 커야 합니다.")
	private BigDecimal cancelAmount; // null이면 전액 취소

	/**
	 * 전액 취소 여부를 반환합니다.
	 *
	 * @return 전액 취소 여부
	 */
	public boolean isFullCancellation() {
		return cancelAmount == null;
	}
}

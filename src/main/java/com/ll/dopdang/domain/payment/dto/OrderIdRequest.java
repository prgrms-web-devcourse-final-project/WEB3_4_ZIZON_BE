package com.ll.dopdang.domain.payment.dto;

import com.ll.dopdang.domain.payment.entity.PaymentType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 주문 ID 생성 요청을 위한 DTO
 */
public record OrderIdRequest(
	@NotNull(message = "결제 유형은 필수입니다")
	PaymentType paymentType, // 결제 유형 (Project: Contract, Order: Order, ETC... )

	@NotNull(message = "참조 ID는 필수입니다")
	Long referenceId, // 참조 ID (Contract, Order 등의 id)

	@Min(value = 1, message = "수량은 1 이상이어야 합니다")
	Integer quantity // 수량 (선택적, 제공된 경우 1 이상)
) {
}

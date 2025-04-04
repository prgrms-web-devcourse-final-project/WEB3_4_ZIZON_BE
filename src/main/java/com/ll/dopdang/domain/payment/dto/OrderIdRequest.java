package com.ll.dopdang.domain.payment.dto;

import com.ll.dopdang.domain.payment.entity.PaymentType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 결제 주문 ID 생성 요청을 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderIdRequest {

	@NotNull(message = "결제 유형은 필수입니다")
	private PaymentType paymentType; // 결제 유형 (Project: Contract, Order: Order, ETC... )

	@NotNull(message = "참조 ID는 필수입니다")
	private Long referenceId; // 참조 ID (Contract, Order 등의 id)
}

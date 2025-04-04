package com.ll.dopdang.domain.payment.dto;

import com.ll.dopdang.domain.payment.entity.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 주문 정보를 저장하기 위한 DTO 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderInfo {

	private PaymentType paymentType;
	private Long referenceId;
}

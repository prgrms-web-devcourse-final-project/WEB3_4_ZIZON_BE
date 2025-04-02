package com.ll.dopdang.domain.payment.entity;

import lombok.Getter;

/**
 * 결제 상태를 나타내는 열거형
 */
@Getter
public enum PaymentStatus {
	PAID("결제 완료"),
	PARTIALLY_CANCELED("부분 취소"),
	FULLY_CANCELED("전체 취소");

	private final String description;

	PaymentStatus(String description) {
		this.description = description;
	}

}

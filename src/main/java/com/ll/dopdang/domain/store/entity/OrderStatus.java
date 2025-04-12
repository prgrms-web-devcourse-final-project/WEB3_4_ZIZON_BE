package com.ll.dopdang.domain.store.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
	PAYMENT_PENDING("결제 진행중"),
	PAID("결제 완료"),
	PREPARING("상품 준비 중"),
	SHIPPING("상품 배송 중"),
	DELIVERED("배송 완료"),
	CANCELLED("주문 취소");

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}
}

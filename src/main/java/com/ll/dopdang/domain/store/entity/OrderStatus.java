package com.ll.dopdang.domain.store.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
	PAID("결제됨"),
	PREPARING("배송 준비 중"),
	SHIPPING("배송 중"),
	DELIVERED("배송 완료"),
	CANCELLED("주문 취소");

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}
}

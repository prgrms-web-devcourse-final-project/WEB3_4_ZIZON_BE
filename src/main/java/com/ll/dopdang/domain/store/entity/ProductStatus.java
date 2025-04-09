package com.ll.dopdang.domain.store.entity;

import lombok.Getter;

@Getter
public enum ProductStatus {
	AVAILABLE("판매 가능"),
	OUT_OF_STOCK("품절"),
	DISCONTINUED("판매 중단");

	private final String description;

	ProductStatus(String description) {
		this.description = description;
	}
}

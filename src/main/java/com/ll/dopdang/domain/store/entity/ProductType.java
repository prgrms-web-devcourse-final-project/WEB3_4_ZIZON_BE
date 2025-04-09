package com.ll.dopdang.domain.store.entity;

import lombok.Getter;

@Getter
public enum ProductType {
	PHYSICAL("리빙"),
	DIGITAL("디지털 컨텐츠");

	private final String description;

	ProductType(String description) {
		this.description = description;
	}
}

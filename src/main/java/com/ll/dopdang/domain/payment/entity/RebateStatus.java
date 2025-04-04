package com.ll.dopdang.domain.payment.entity;

import lombok.Getter;

/**
 * 정산 상태를 나타내는 열거형
 */
@Getter
public enum RebateStatus {
	PENDING("정산 대기"),
	COMPLETED("정산 완료"),
	FAILED("정산 실패"),
	HELD("정산 보류");

	private final String description;

	RebateStatus(String description) {
		this.description = description;
	}
}

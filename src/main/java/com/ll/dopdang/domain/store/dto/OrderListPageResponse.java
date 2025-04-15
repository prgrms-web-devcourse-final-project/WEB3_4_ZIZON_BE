package com.ll.dopdang.domain.store.dto;

import java.util.List;

public record OrderListPageResponse(
	List<OrderResponse> orders,
	int currentPage,
	int pageSize,
	boolean hasNext
) {
	public static OrderListPageResponse of(List<OrderResponse> orders, int currentPage, int pageSize, boolean hasNext) {
		return new OrderListPageResponse(orders, currentPage, pageSize, hasNext);
	}
}

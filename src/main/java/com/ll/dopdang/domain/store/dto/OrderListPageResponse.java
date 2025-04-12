package com.ll.dopdang.domain.store.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListPageResponse {
	private List<OrderResponse> orders;
	private int currentPage;
	private int pageSize;
	private boolean hasNext;

	public static OrderListPageResponse of(List<OrderResponse> orders, int currentPage, int pageSize, boolean hasNext) {
		return OrderListPageResponse.builder()
			.orders(orders)
			.currentPage(currentPage)
			.pageSize(pageSize)
			.hasNext(hasNext)
			.build();
	}
}

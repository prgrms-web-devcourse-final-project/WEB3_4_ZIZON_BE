package com.ll.dopdang.domain.store.dto;

import java.util.List;

public record ProductListPageResponse(
	List<ProductListResponse> products,
	int currentPage,
	int pageSize,
	boolean hasNext
) {
	public static ProductListPageResponse of(List<ProductListResponse> products, int currentPage, int pageSize, boolean hasNext) {
		return new ProductListPageResponse(products, currentPage, pageSize, hasNext);
	}
}

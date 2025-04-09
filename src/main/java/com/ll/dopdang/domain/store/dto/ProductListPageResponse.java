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
public class ProductListPageResponse {
	private List<ProductListResponse> products;
	private int currentPage;
	private int pageSize;
	private boolean hasNext;
}

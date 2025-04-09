package com.ll.dopdang.domain.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductAllResponse {
	private Long expertId;
	private Long categoryId;
	private String description;
	private Integer stock;
	private String productType;
	private String thumbnailImage;

}

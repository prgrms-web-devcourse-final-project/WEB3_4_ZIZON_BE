package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.entity.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListResponse {
	private Long id;
	private String name;
	private String description;
	private BigDecimal price;
	private Integer stock;
	private ProductType productType;
	private String thumbnailUrl;
	private String expertName;
	private String categoryName;
	private LocalDateTime createdAt;

	public static ProductListResponse of(Product product) {
		return ProductListResponse.builder()
			.id(product.getId())
			.name(product.getTitle())
			.description(product.getDescription())
			.price(product.getPrice())
			.stock(product.getStock())
			.productType(product.getProductType())
			.thumbnailUrl(product.getThumbnailImage())
			.expertName(product.getExpert().getMember().getName())
			.categoryName(product.getCategory().getName())
			.createdAt(product.getCreatedAt())
			.build();
	}
}


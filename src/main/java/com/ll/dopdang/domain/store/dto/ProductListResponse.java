package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.entity.ProductType;

public record ProductListResponse(
	Long id,
	String name,
	String description,
	BigDecimal price,
	Integer stock,
	ProductType productType,
	String thumbnailUrl,
	String expertName,
	String categoryName,
	LocalDateTime createdAt
) {
	public static ProductListResponse of(Product product) {
		return new ProductListResponse(
			product.getId(),
			product.getTitle(),
			product.getDescription(),
			product.getPrice(),
			product.getStock(),
			product.getProductType(),
			product.getThumbnailImage(),
			product.getExpert().getMember().getName(),
			product.getCategory().getName(),
			product.getCreatedAt()
		);
	}
}

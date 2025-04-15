package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.entity.ProductType;

public record ProductDetailResponse(
	Long id,
	String expertName,
	String title,
	String description,
	BigDecimal price,
	Integer stock,
	ProductType productType,
	String thumbnailImage,
	LocalDateTime createAt,
	List<DigitalContentDetailResponse> digitalContents
) {
	public static ProductDetailResponse of(Product product) {
		return new ProductDetailResponse(
			product.getId(),
			product.getExpert().getMember().getName(),
			product.getTitle(),
			product.getDescription(),
			product.getPrice(),
			product.getStock(),
			product.getProductType(),
			product.getThumbnailImage(),
			product.getCreatedAt(),
			null
		);
	}

	public static ProductDetailResponse of(Product product, List<DigitalContentDetailResponse> digitalContents) {
		return new ProductDetailResponse(
			product.getId(),
			product.getExpert().getMember().getName(),
			product.getTitle(),
			product.getDescription(),
			product.getPrice(),
			product.getStock(),
			product.getProductType(),
			product.getThumbnailImage(),
			product.getCreatedAt(),
			digitalContents
		);
	}
}

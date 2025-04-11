package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.entity.ProductType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDetailResponse {
	private Long id;
	private String expertName;
	private String title;
	private String description;
	private BigDecimal price;
	private Integer stock;
	private ProductType productType;
	private String thumbnailImage;
	private LocalDateTime createAt;
	private List<DigitalContentDetailResponse> digitalContents;

	public static ProductDetailResponse of(Product product) {
		return ProductDetailResponse.builder()
			.id(product.getId())
			.expertName(product.getExpert().getMember().getName())
			.title(product.getTitle())
			.description(product.getDescription())
			.price(product.getPrice())
			.stock(product.getStock())
			.productType(product.getProductType())
			.thumbnailImage(product.getThumbnailImage())
			.createAt(product.getCreatedAt())
			.build();
	}

	public static ProductDetailResponse of(Product product, List<DigitalContentDetailResponse> digitalContents) {
		return ProductDetailResponse.builder()
			.id(product.getId())
			.expertName(product.getExpert().getMember().getName())
			.title(product.getTitle())
			.description(product.getDescription())
			.price(product.getPrice())
			.stock(product.getStock())
			.productType(product.getProductType())
			.thumbnailImage(product.getThumbnailImage())
			.createAt(product.getCreatedAt())
			.digitalContents(digitalContents)
			.build();
	}
}


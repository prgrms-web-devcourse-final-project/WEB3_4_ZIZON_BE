package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.util.List;

import com.ll.dopdang.domain.store.entity.ProductType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {
	@NotNull(message = "카테고리 ID는 필수입니다.")
	private Long categoryId;

	@NotBlank(message = "제품 제목은 필수입니다.")
	@Size(max = 200, message = "제품 제목은 최대 200자까지 입력 가능합니다.")
	private String title;

	@Size(max = 1000, message = "제품 설명은 최대 1000자까지 입력 가능합니다.")
	private String description;

	@NotNull(message = "가격은 필수입니다.")
	@DecimalMin(value = "0.0", inclusive = true, message = "가격은 0 이상이어야 합니다.")
	private BigDecimal price;

	@NotNull(message = "재고는 필수입니다.")
	@Min(value = -1, message = "재고는 -1(무제한) 이상이어야 합니다.")
	private Integer stock;

	@NotNull(message = "제품 타입은 필수입니다.")
	private ProductType productType;

	private String thumbnailImage;
	
	private List<DigitalContentRequest> digitalContents;
}

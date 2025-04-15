package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProductUpdateRequest(
	@NotBlank(message = "제품 제목은 필수입니다.")
	@Size(max = 200, message = "제품 제목은 최대 200자까지 입력 가능합니다.")
	String title,

	@Size(max = 20000, message = "제품 설명은 최대 20000자까지 입력 가능합니다.")
	String description,

	@DecimalMin(value = "0.0", message = "가격은 0 이상이어야 합니다.")
	BigDecimal price,

	@Min(value = -1, message = "재고는 -1(무제한) 이상이어야 합니다.")
	Integer stock,

	String thumbnailImage,

	List<DigitalContentUpdateRequest> digitalContents
) {
}

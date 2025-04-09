package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequest {

	@NotNull(message = "프로젝트 ID는 필수입니다.")
	private Long projectId;

	@NotNull(message = "평점은 필수입니다.")
	@DecimalMin(value = "1.0", inclusive = true, message = "최소 평점은 1.0입니다.")
	@DecimalMax(value = "5.0", inclusive = true, message = "최대 평점은 5.0입니다.")
	private BigDecimal score;

	@NotBlank(message = "내용은 비어 있을 수 없습니다.")
	@Size(max = 1000, message = "내용은 최대 1000자까지 입력 가능합니다.")
	private String content;

	private String imageUrl; // 선택값
}

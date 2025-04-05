package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfferCreateRequest {
	@NotNull(message = "가격은 필수입니다.")
	private BigDecimal price;

	private String description;

	@NotNull(message = "배송일은 필수입니다.")
	private int deliveryDays;
}

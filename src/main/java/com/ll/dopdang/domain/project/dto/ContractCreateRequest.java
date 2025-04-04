package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreateRequest {
	private Long offerId;
	private BigDecimal price;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}

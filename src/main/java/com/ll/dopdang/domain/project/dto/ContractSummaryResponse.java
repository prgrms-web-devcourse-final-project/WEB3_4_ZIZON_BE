package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractSummaryResponse {

	private Long contractId;
	private Long projectId;
	private String projectTitle;
	private String clientName;
	private BigDecimal price;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String status;
	//방어적 복사 객체지향
	// 	public static ContractSummaryResponse of(Offer offer, Project project, String thumbnailImageUrl) {
	// 		return ContractSummaryResponse.builder().contractId(offer.expert());
	// 	}
	// }
}

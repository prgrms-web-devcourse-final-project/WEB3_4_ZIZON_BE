package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.project.entity.Contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDetailResponse {

	private Long contractId;
	private Long projectId;
	private String projectTitle;
	private String clientName;
	private String expertName;
	private BigDecimal price;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String status;

	public static ContractDetailResponse from(Contract contract) {
		return ContractDetailResponse.builder()
			.contractId(contract.getId())
			.projectId(contract.getProject().getId())
			.projectTitle(contract.getProject().getTitle())
			.clientName(contract.getClient().getName())
			.expertName(contract.getExpert().getMember().getName()) // Expert 내부에 Member가 있을 경우
			.price(contract.getPrice())
			.startDate(contract.getStartDate())
			.endDate(contract.getEndDate())
			.status(contract.getStatus().name())
			.build();
	}
}

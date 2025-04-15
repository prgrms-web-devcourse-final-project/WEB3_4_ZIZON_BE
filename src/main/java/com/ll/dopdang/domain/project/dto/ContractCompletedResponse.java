package com.ll.dopdang.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "계약완료 응답 DTO")
public class ContractCompletedResponse {

	@Schema(description = "완료된 컨트랙트 ID", example = "42")
	private Long contractId;

	@Schema(description = "응답 메시지", example = "계약한 의뢰가 성공적으로 완료되었습니다. (ID: 42)")
	private String message;

	public static ContractCompletedResponse of(Long contractId) {
		return new ContractCompletedResponse(
			contractId,
			"계약한 의뢰가 성공적으로 완료되었습니다. (ID: " + contractId + ")"
		);
	}
}

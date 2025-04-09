package com.ll.dopdang.domain.expert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertCreateResponseDto {
	private Long expertId; // 자격증 ID
	private String message; // 자격증 이름
}

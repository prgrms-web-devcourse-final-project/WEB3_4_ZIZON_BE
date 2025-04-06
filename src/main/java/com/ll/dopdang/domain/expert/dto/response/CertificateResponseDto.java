package com.ll.dopdang.domain.expert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponseDto {

	private Long id; // 자격증 ID
	private String name; // 자격증 이름
}
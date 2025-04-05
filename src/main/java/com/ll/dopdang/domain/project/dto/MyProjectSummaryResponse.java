package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마이페이지 - 클라이언트가 등록한 프로젝트 단일 요약 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyProjectSummaryResponse {

	private Long id; // 프로젝트 ID
	private String title; // 제목
	private String summary; // 한 줄 요약
	private String region; // 지역
	private BigDecimal budget; // 예산
	private String status; // 프로젝트 상태
	private LocalDateTime deadline; // 마감일

	private String thumbnailImageUrl; // 대표 이미지 URL (없으면 기본 이미지)

	private Long contractId; // 연결된 계약 ID (없으면 null)
}

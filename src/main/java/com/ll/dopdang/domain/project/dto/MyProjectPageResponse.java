package com.ll.dopdang.domain.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마이페이지 - 클라이언트가 등록한 프로젝트 목록 응답 DTO
 * Offset 기반 무한 스크롤을 고려한 구조.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyProjectPageResponse {

	private List<MyProjectSummaryResponse> projects;

	private int currentPage;   // 현재 페이지 번호 (0부터 시작)
	private int pageSize;      // 한 페이지 당 항목 수
	private boolean hasNext;   // 다음 페이지 존재 여부
}

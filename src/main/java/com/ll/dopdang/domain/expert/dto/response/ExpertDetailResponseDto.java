package com.ll.dopdang.domain.expert.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDetailResponseDto {
	private Long id;              // 전문가 ID
	private String name;          // 전문가 이름 (Member)
	private String categoryName; // 대분류 카테고리 이름
	private List<String> subCategoryNames; // 소분류 카테고리 이름 목록
	private String introduction; // 자기소개
	private int careerYears;      // 경력 연수
	private Boolean gender;       // 성별 (0 = 남자, 1 = 여자)
	private String profileImage;
	private Long mainCategoryId;
	private List<Long> subCategoryIds;
	private List<String> certificateNames;
	private String bankName; // 은행명
	private String accountNumber; // 계좌번호

	private String portfolioTitle;
	private String portfolioImage;
}

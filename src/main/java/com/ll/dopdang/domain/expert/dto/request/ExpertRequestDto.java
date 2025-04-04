package com.ll.dopdang.domain.expert.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExpertRequestDto {

	private String categoryName; // 대분류 카테고리 이름
	private List<String> subCategoryNames; // 소분류 카테고리 이름들
	private int careerYears; // 경력 연수
	private String introduction; // 자기소개
	private String certification; // 자격증 정보
	private Boolean gender; // 0 = 남자, 1 = 여자
	private String bankName; // 은행명
	private String accountNumber; // 계좌번호
	private String sellerInfo; // 판매자 정보
}

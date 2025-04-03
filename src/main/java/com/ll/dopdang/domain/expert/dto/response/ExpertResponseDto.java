package com.ll.dopdang.domain.expert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertResponseDto {

    private String name; // 전문가 이름
    private String mainCategoryName; // 대분류 카테고리 이름
    private int careerYears; // 경력 연수
    private String introduction; // 자기소개
}

package com.ll.dopdang.domain.expert.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpertUpdateRequestDto {
    private String categoryName; // 대분류 카테고리 이름 (Optional)
    private List<String> subCategoryNames; // 소분류 카테고리 이름 리스트 (Optional)
    private int careerYears; // 경력 연수
    private String introduction; // 자기소개
    private String bankName; // 은행명
    private String accountNumber; // 계좌번호
    private String sellerInfo; // 판매자 정보 (Optional)
    private List<String> certificateNames;
}

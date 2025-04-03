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

    private String introduction;

    private int careerYears;

    private String categoryName;

    private String certification;

    private String name;

    private String bankName;

    private Boolean gender;

    private String accountNumber;

}

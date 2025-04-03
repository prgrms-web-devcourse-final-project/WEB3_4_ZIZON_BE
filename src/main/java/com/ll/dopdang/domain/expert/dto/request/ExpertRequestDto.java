package com.ll.dopdang.domain.expert.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExpertRequestDto {

    @NotBlank(message = "소개는 필수 항목입니다")
    @Size(max = 500, message = "소개는 최대 500자까지 가능합니다.")
    private String introduction;

    @NotBlank(message = "경력 연수는 필수 항목입니다.")
    private int careerYears;

    private String certificatation;

    private Integer categoryId;

    private Boolean gender;

    @NotBlank(message = "계좌 정보는 필수 항목입니다.")
    private String bankName;

    @NotBlank(message = "계좌 정보는 필수 항목입니다.")
    private String accountNumber;

}

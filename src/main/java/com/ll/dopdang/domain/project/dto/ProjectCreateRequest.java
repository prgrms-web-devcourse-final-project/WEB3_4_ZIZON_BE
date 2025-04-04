package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectCreateRequest {

	@NotNull(message = "카테고리 ID는 필수입니다.")
	private Integer categoryId;

	@NotBlank(message = "프로젝트 제목은 필수입니다.")
	private String title;

	@NotBlank(message = "프로젝트 한줄 요약은 필수입니다.")
	@Schema(description = "프로젝트 한줄 요약", example = "강아지를 위한 스마트 급식기 개발")
	private String summary;

	@NotBlank(message = "프로젝트 설명을 적어주세요.")
	@Schema(description = "프로젝트 상세 설명", example = "강아지를 집에 혼자 둘 때에도 사료를 시간에 맞춰 줄 수 있는 스마트 급식기를 만들고자 합니다.")
	private String description;

	@NotBlank(message = "지역 정보는 필수입니다.")
	@Schema(description = "프로젝트 진행 지역", example = "서울특별시 강남구")
	private String region;

	@PositiveOrZero(message = "예산은 0 이상이어야 합니다.")
	@Schema(description = "프로젝트 예산", example = "500000")
	private BigDecimal budget;

	@FutureOrPresent(message = "마감일은 현재 시각 이후여야 합니다.")
	@Schema(description = "프로젝트 마감일", example = "2025-06-30T23:59:59")
	private LocalDateTime deadline;

	// 없을 수도 있음
	@Schema(description = "선택 항목: 지정 전문가 ID", example = "42")
	private Long expertId;

	@Schema(description = "프로젝트 이미지 URL 리스트", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
	private List<@NotBlank String> imageUrls;
}

package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;

import com.ll.dopdang.domain.review.entity.ReviewStats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReviewStatsResponse {
	private BigDecimal averageScore;
	private int reviewCount;

	public static ReviewStatsResponse from(ReviewStats stats) {
		return ReviewStatsResponse.builder()
			.averageScore(stats.getAverageScore())
			.reviewCount(stats.getReviewCount())
			.build();
	}
}

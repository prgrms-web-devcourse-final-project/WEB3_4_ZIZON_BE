package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreateResponse {
	private Long id; // 리뷰 ID
	private String reviewerName;
	private BigDecimal score;
	private LocalDateTime createdAt;

	public static ReviewCreateResponse from(Review review) {
		return ReviewCreateResponse.builder()
			.id(review.getId())
			.reviewerName(review.getReviewer().getName())
			.score(review.getScore())
			.createdAt(review.getCreatedAt())
			.build();
	}
}
package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.review.entity.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertReviewResponse {

	private Long reviewId;
	private BigDecimal score;
	private String content;
	private String imageUrl;
	private String reviewerName;
	private String reviewerProfileImage;
	private LocalDateTime createdAt;

	public static ExpertReviewResponse from(Review review) {
		return ExpertReviewResponse.builder()
			.reviewId(review.getId())
			.score(review.getScore())
			.content(review.getContent())
			.imageUrl(review.getImageUrl())
			.reviewerName(review.getReviewer().getName())
			.reviewerProfileImage(review.getReviewer().getProfileImage())
			.createdAt(review.getCreatedAt())
			.build();
	}
}

package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.review.entity.Review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewDetailResponse {
	private Long id;
	private String reviewerName;
	private String reviewerProfileImage;
	private BigDecimal score;
	private String content;
	private String imageUrl;
	private LocalDateTime createdAt;

	public static ReviewDetailResponse from(Review review) {
		Member reviewer = review.getReviewer();
		return ReviewDetailResponse.builder()
			.id(review.getId())
			.reviewerName(reviewer.getName())
			.reviewerProfileImage(reviewer.getProfileImage())
			.score(review.getScore())
			.content(review.getContent())
			.imageUrl(review.getImageUrl())
			.createdAt(review.getCreatedAt())
			.build();
	}
}

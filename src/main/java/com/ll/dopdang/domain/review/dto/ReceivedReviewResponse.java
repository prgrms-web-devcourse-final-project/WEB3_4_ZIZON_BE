package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceivedReviewResponse {
	// 내가 받은 리뷰 응답
	private Long reviewId;
	private BigDecimal score;
	private String content;
	private String imageUrl;
	private LocalDateTime createdAt;

	private Long reviewerId;
	private String reviewerName;
	private String reviewerProfileImage;

	private Long projectId;
	private String projectTitle;
}

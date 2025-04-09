package com.ll.dopdang.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WrittenReviewResponse {
	// 내가 쓴 리뷰 응답
	private Long reviewId;
	private BigDecimal score;
	private String content;
	private String imageUrl;
	private LocalDateTime createdAt;

	private Long expertId;
	private String expertName;
	private String expertProfileImage;

	private Long projectId;
	private String projectTitle;
}

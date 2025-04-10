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
public class ClientReviewResponse {

	private Long reviewId;
	private BigDecimal score;
	private String content;
	private String imageUrl;
	private String expertName;
	private String expertProfileImage;
	private LocalDateTime createdAt;

	public static ClientReviewResponse from(Review review) {
		return ClientReviewResponse.builder()
			.reviewId(review.getId())
			.score(review.getScore())
			.content(review.getContent())
			.imageUrl(review.getImageUrl())
			.expertName(review.getContract().getExpert().getMember().getName())
			.expertProfileImage(review.getContract().getExpert().getMember().getProfileImage())
			.createdAt(review.getCreatedAt())
			.build();
	}
}

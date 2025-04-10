package com.ll.dopdang.domain.review.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ll.dopdang.domain.review.entity.Review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientReviewPageResponse {

	private List<ClientReviewResponse> reviews;
	private int currentPage;
	private int size;
	private boolean hasNext;

	public static ClientReviewPageResponse from(Page<Review> page) {
		List<ClientReviewResponse> content = page.getContent().stream()
			.map(ClientReviewResponse::from)
			.toList();

		return ClientReviewPageResponse.builder()
			.reviews(content)
			.currentPage(page.getNumber())
			.size(page.getSize())
			.hasNext(page.hasNext())
			.build();
	}
}

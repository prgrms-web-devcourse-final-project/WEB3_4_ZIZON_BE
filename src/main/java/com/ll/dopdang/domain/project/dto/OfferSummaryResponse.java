package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.project.entity.Offer;
import com.ll.dopdang.domain.project.entity.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferSummaryResponse {

	private Long offerId;
	private String offerStatus; // pending, accepted, rejected
	private BigDecimal price;
	private Integer deliveryDays;

	private Long projectId;
	private String title;
	private String summary;
	private String region;
	private String status; // 프로젝트 상태 (OPEN, IN_PROGRESS 등)
	private LocalDateTime deadline;

	// 의뢰인 정보
	private String clientName;
	private String clientProfileImageUrl;

	private String thumbnailImageUrl;

	public static OfferSummaryResponse of(Offer offer, Project project, String thumbnailImageUrl) {
		Member client = project.getClient();

		return OfferSummaryResponse.builder()
			.offerId(offer.getId())
			.offerStatus(String.valueOf(offer.getStatus()))
			.price(offer.getPrice())
			.deliveryDays(offer.getDeliveryDays())

			.projectId(project.getId())
			.title(project.getTitle())
			.summary(project.getSummary())
			.region(project.getRegion())
			.status(project.getStatus().name())
			.deadline(project.getDeadline())

			.clientName(client.getName())
			.clientProfileImageUrl(client.getProfileImage())
			.thumbnailImageUrl(thumbnailImageUrl)
			.build();
	}
}

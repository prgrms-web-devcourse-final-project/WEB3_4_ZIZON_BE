package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.project.entity.Offer;

import lombok.Getter;

@Getter
public class OfferDetailResponse {
	private final Long id;
	private final Long projectId;
	private final Long expertId;
	private final BigDecimal price;
	private final String description;
	private final int deliveryDays;
	private final Offer.OfferStatus status;
	private final LocalDateTime createAt;

	public OfferDetailResponse(Offer offer) {
		this.id = offer.getId();
		this.projectId = offer.getProject().getId();
		this.expertId = offer.getExpert().getId();
		this.price = offer.getPrice();
		this.description = offer.getDescription();
		this.deliveryDays = offer.getDeliveryDays();
		this.status = offer.getStatus();
		this.createAt = offer.getCreatedAt();
	}
	public static OfferDetailResponse from(Offer offer) {
		return new OfferDetailResponse(offer);
	}
}

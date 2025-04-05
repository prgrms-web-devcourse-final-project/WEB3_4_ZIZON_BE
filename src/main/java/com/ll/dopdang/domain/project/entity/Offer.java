package com.ll.dopdang.domain.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.global.entity.BaseEntity;
import com.ll.dopdang.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Offer extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expert_id", nullable = false)
	private Expert expert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(name = "delivery_days", nullable = false)
	private Integer deliveryDays;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OfferStatus status; // 예: PENDING, ACCEPTED, REJECTED

	// OfferStatus enum for status field
	public enum OfferStatus {
		PENDING,
		ACCEPTED,
		REJECTED
	}
}

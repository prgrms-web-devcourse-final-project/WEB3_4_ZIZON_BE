package com.ll.dopdang.domain.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.expert.entity.Expert;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Table(name = "contract")
@NoArgsConstructor
@SuperBuilder
public class Contract {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "offer_id")
	private Offer offer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id")
	private Member client;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expert_id")
	private Expert expert;

	private BigDecimal price;

	@Column(name = "start_date")
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Enumerated(EnumType.STRING)
	private ContractStatus status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * 오퍼 정보로부터 계약을 생성하는 정적 팩토리 메서드
	 *
	 * @param offer 오퍼 정보
	 * @param price 계약 가격
	 * @param startDate 계약 시작일
	 * @param endDate 계약 종료일
	 * @return 생성된 Contract 엔티티
	 */
	public static Contract createFromOffer(Offer offer, BigDecimal price, LocalDateTime startDate,
		LocalDateTime endDate) {
		return Contract.builder()
			.project(offer.getProject())
			.offer(offer)
			.client(offer.getProject().getClient())
			.expert(offer.getExpert())
			.price(price)
			.startDate(startDate)
			.endDate(endDate)
			.status(ContractStatus.PENDING)
			.build();
	}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public void updateStatus(ContractStatus contractStatus) {
		this.status = contractStatus;
	}

	// ContractStatus enum for status field
	public enum ContractStatus {
		PENDING,
		IN_PROGRESS,
		COMPLETED,
		CANCELLED,
		DISPUTED
	}
}

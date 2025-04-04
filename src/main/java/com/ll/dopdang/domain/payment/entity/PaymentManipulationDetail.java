package com.ll.dopdang.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 금액 조작 감지 상세 정보를 저장하는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment_manipulation_detail")
public class PaymentManipulationDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Column(name = "expected_amount", precision = 10, scale = 2)
	private BigDecimal expectedAmount;

	@Column(name = "requested_amount", precision = 10, scale = 2)
	private BigDecimal requestedAmount;

	@Column(name = "detected_at", nullable = false)
	private LocalDateTime detectedAt;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "user_agent")
	private String userAgent;

	/**
	 * 결제 조작 세부 정보 생성 팩토리 메서드
	 */
	public static PaymentManipulationDetail create(
		BigDecimal expectedAmount,
		BigDecimal requestedAmount,
		String ipAddress,
		String userAgent) {

		return PaymentManipulationDetail.builder()
			.expectedAmount(expectedAmount)
			.requestedAmount(requestedAmount)
			.detectedAt(LocalDateTime.now())
			.ipAddress(ipAddress)
			.userAgent(userAgent)
			.build();
	}

	/**
	 * 결제 엔티티와 연결합니다.
	 */
	public void setPayment(Payment payment) {
		this.payment = payment;
	}
}

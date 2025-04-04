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
 * 결제 취소 상세 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "payment_cancellation_detail")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCancellationDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Column(name = "cancel_amount", precision = 10, scale = 2)
	private BigDecimal cancelAmount;

	@Column(name = "cancel_reason")
	private String cancelReason;

	@Column(name = "cancellation_date")
	private LocalDateTime cancellationDate;

	@Column(name = "is_full_cancellation")
	private boolean fullCancellation;

	/**
	 * 결제 취소 상세 정보를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param cancelAmount 취소 금액
	 * @param cancelReason 취소 사유
	 * @param isFullCancellation 전액 취소 여부
	 * @return 생성된 PaymentCancellationDetail 엔티티
	 */
	public static PaymentCancellationDetail createCancellationDetail(
		Payment payment,
		BigDecimal cancelAmount,
		String cancelReason,
		boolean isFullCancellation) {

		PaymentCancellationDetail detail = PaymentCancellationDetail.builder()
			.cancelAmount(cancelAmount)
			.cancelReason(cancelReason)
			.cancellationDate(LocalDateTime.now())
			.fullCancellation(isFullCancellation)
			.build();

		payment.addCancellationDetail(detail);
		return detail;
	}

	/**
	 * 결제 정보를 설정합니다.
	 * Payment 엔티티와의 양방향 관계를 위해 사용됩니다.
	 */
	void setPayment(Payment payment) {
		this.payment = payment;
	}
}

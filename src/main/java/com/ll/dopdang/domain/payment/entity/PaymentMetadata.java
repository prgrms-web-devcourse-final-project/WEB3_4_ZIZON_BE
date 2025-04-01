package com.ll.dopdang.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_metadata")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Lob
	@Column(name = "metadata", columnDefinition = "TEXT")
	private String metadata;

	/**
	 * 결제 메타데이터를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param metadata 메타데이터 (JSON 문자열)
	 * @return 생성된 PaymentMetadata 엔티티
	 */
	public static PaymentMetadata createMetadata(Payment payment, String metadata) {
		PaymentMetadata paymentMetadata = PaymentMetadata.builder()
			.metadata(metadata)
			.build();

		payment.setMetadata(paymentMetadata);
		return paymentMetadata;
	}

	/**
	 * 결제 정보를 설정합니다.
	 * Payment 엔티티와의 양방향 관계를 위해 사용됩니다.
	 */
	void setPayment(Payment payment) {
		this.payment = payment;
	}
}

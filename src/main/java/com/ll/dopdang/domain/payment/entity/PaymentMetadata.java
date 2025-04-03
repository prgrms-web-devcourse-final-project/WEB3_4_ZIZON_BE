package com.ll.dopdang.domain.payment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)  // OneToOne에서 ManyToOne으로 변경
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Column(name = "metadata_type")
	@Enumerated(EnumType.STRING)
	private MetadataType type;  // 메타데이터 유형 추가 (PAYMENT, CANCELLATION)

	@Lob
	@Column(name = "metadata", columnDefinition = "TEXT")
	private String metadata;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * 결제 메타데이터를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param metadata 메타데이터 (JSON 문자열)
	 * @return 생성된 PaymentMetadata 엔티티
	 */
	public static PaymentMetadata createPaymentMetadata(Payment payment, String metadata) {
		PaymentMetadata paymentMetadata = PaymentMetadata.builder()
			.payment(payment)
			.type(MetadataType.PAYMENT)
			.metadata(metadata)
			.createdAt(LocalDateTime.now())
			.build();

		payment.addMetadata(paymentMetadata);
		return paymentMetadata;
	}

	/**
	 * 취소 메타데이터를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param metadata 메타데이터 (JSON 문자열)
	 * @return 생성된 PaymentMetadata 엔티티
	 */
	public static PaymentMetadata createCancellationMetadata(Payment payment, String metadata) {
		PaymentMetadata cancellationMetadata = PaymentMetadata.builder()
			.payment(payment)
			.type(MetadataType.CANCELLATION)
			.metadata(metadata)
			.createdAt(LocalDateTime.now())
			.build();

		payment.addMetadata(cancellationMetadata);
		return cancellationMetadata;
	}

	/**
	 * 결제 실패 메타데이터를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param metadata 메타데이터 (JSON 문자열)
	 * @return 생성된 PaymentMetadata 엔티티
	 */
	public static PaymentMetadata createFailedPaymentMetadata(Payment payment, String metadata) {
		PaymentMetadata paymentMetadata = PaymentMetadata.builder()
			.payment(payment)
			.type(MetadataType.FAILED)
			.metadata(metadata)
			.createdAt(LocalDateTime.now())
			.build();

		payment.addMetadata(paymentMetadata);
		return paymentMetadata;
	}

	/**
	 * 결제 실패 메타데이터를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param metadata 메타데이터 (JSON 문자열)
	 * @return 생성된 PaymentMetadata 엔티티
	 */
	public static PaymentMetadata createViolatedPaymentMetadata(Payment payment, String metadata) {
		PaymentMetadata paymentMetadata = PaymentMetadata.builder()
			.payment(payment)
			.type(MetadataType.VIOLATED)
			.metadata(metadata)
			.createdAt(LocalDateTime.now())
			.build();

		payment.addMetadata(paymentMetadata);
		return paymentMetadata;
	}

	/**
	 * 결제 정보를 설정합니다.
	 * Payment 엔티티와의 양방향 관계를 위해 사용됩니다.
	 */
	void setPayment(Payment payment) {
		this.payment = payment;
	}

	/**
	 * 메타데이터 유형
	 */
	public enum MetadataType {
		PAYMENT,      // 결제 성공 메타데이터
		CANCELLATION, // 결제 취소 메타데이터
		FAILED,       // 결제 인증 실패 메타데이터
		VIOLATED      // 결제 검증 실패 메타데이터(금액 변조 등)
	}
}

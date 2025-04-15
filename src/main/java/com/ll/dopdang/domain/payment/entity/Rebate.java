package com.ll.dopdang.domain.payment.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.ll.dopdang.domain.expert.entity.Expert;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 정산 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "rebate")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rebate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expert_id")
	private Expert expert;

	@Column(name = "rebate_year_month")
	private String rebateYearMonth; // 정산 년월 (YYYY-MM 형식)

	@Column(name = "original_amount", precision = 10, scale = 2)
	private BigDecimal originalAmount; // 원래 결제 금액

	@Column(name = "canceled_amount", precision = 10, scale = 2)
	private BigDecimal canceledAmount; // 취소된 금액

	@Column(name = "fee_amount", precision = 10, scale = 2)
	private BigDecimal feeAmount; // 수수료 금액

	@Column(name = "rebate_amount", precision = 10, scale = 2)
	private BigDecimal rebateAmount; // 정산 금액 (수수료 제외)

	@Column(name = "payment_date")
	private LocalDateTime paymentDate; // 원 결제일

	@Column(name = "rebate_date")
	private LocalDateTime rebateDate; // 정산 처리일

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private RebateStatus status; // 정산 상태

	@Column(name = "payment_type")
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType; // 결제 유형

	@Column(name = "reference_id")
	private Long referenceId; // 참조 ID (계약 ID 등)

	@Column(name = "items_summary")
	private String itemsSummary; // 결제 항목 요약

	/**
	 * Payment 엔티티로부터 Rebate 엔티티를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param rebateYearMonth 정산 년월
	 * @return 생성된 Rebate 엔티티
	 */
	public static Rebate createFromPayment(Payment payment, BigDecimal feeRate, Expert expert,
		YearMonth rebateYearMonth) {
		// 남은 결제 금액 (부분 취소 반영)
		BigDecimal remainingAmount = payment.getRemainingAmount();

		// 수수료 계산 (남은 금액의 10%)
		BigDecimal feeAmount = remainingAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);

		// 정산 금액 계산 (남은 금액 - 수수료)
		BigDecimal rebateAmount = remainingAmount.subtract(feeAmount);

		return Rebate.builder()
			.payment(payment)
			.expert(expert)
			.rebateYearMonth(rebateYearMonth.toString())
			.originalAmount(payment.getTotalPrice())
			.canceledAmount(payment.getCanceledAmount())
			.feeAmount(feeAmount)
			.rebateAmount(rebateAmount)
			.paymentDate(payment.getPaymentDate())
			.rebateDate(null) // 정산 처리 시 설정
			.status(RebateStatus.PENDING)
			.paymentType(payment.getPaymentType())
			.referenceId(payment.getReferenceId())
			.itemsSummary(payment.getItemsSummary())
			.build();
	}

	/**
	 * 정산 처리를 완료하는 메서드
	 */
	public void complete() {
		this.status = RebateStatus.COMPLETED;
		this.rebateDate = LocalDateTime.now();
	}

	/**
	 * 정산 처리를 실패로 표시하는 메서드
	 */
	public void fail() {
		this.status = RebateStatus.FAILED;
	}

	/**
	 * 정산 처리를 보류로 표시하는 메서드
	 */
	public void hold() {
		this.status = RebateStatus.HELD;
	}

	/**
	 * 정산 대상 여부를 확인하는 메서드
	 *
	 * @return 정산 대상 여부
	 */
	public boolean isEligibleForRebate() {
		return !payment.isCanceled() && status == RebateStatus.PENDING;
	}

	/**
	 * 총 금액을 반환하는 메서드 (정산 금액 + 수수료)
	 *
	 * @return 총 금액
	 */
	public BigDecimal getTotalAmount() {
		return rebateAmount.add(feeAmount);
	}
}

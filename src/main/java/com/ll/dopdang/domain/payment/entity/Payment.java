package com.ll.dopdang.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.project.entity.Contract;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_payment_reference",
			columnNames = {"payment_type", "reference_id"}
		)
	})
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(name = "payment_key")
	private String paymentKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_type")
	private PaymentType paymentType;

	@Column(name = "reference_id")
	private Long referenceId;

	@Column(name = "items_summary")
	private String itemsSummary;

	@Column(name = "items_count")
	private Integer itemsCount;

	@Column(name = "installment_months")
	private Integer installmentMonths;

	@Column(name = "total_price", precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@Column(name = "total_fee", precision = 10, scale = 2)
	private BigDecimal totalFee;

	@Column(name = "payment_date")
	private LocalDateTime paymentDate;

	@Builder.Default
	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentDetail> paymentDetails = new ArrayList<>();

	@OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	private PaymentMetadata metadata;

	/**
	 * 계약 정보로부터 결제 정보를 생성하는 정적 팩토리 메서드
	 *
	 * @param contract 계약 정보
	 * @param amount 결제 금액
	 * @param fee 수수료
	 * @param paymentKey 결제 키
	 * @return 생성된 Payment 엔티티
	 */
	public static Payment createFromContract(Contract contract, BigDecimal amount, BigDecimal fee, String paymentKey) {
		return Payment.builder()
			.member(contract.getClient())
			.paymentKey(paymentKey)
			.paymentType(PaymentType.PROJECT)
			.referenceId(contract.getId())
			.itemsSummary(contract.getProject().getTitle())
			.itemsCount(1)
			.installmentMonths(1)
			.totalPrice(amount)
			.totalFee(fee)
			.paymentDate(LocalDateTime.now())
			.build();
	}

	/**
	 * 결제 상세 정보를 추가합니다.
	 */
	public void addPaymentDetail(PaymentDetail paymentDetail) {
		this.paymentDetails.add(paymentDetail);
		paymentDetail.setPayment(this);
	}

	/**
	 * 결제 메타데이터를 설정합니다.
	 */
	public void setMetadata(PaymentMetadata metadata) {
		this.metadata = metadata;
		metadata.setPayment(this);
	}
}

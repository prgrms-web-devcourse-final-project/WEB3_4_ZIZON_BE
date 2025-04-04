package com.ll.dopdang.domain.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
			columnNames = {"payment_type", "reference_id"})
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

	// 결제 상태 필드 추가
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	@Builder.Default
	private PaymentStatus status = PaymentStatus.PAID;

	// 취소된 총 금액 (부분 취소 시 누적)
	@Column(name = "canceled_amount", precision = 10, scale = 2)
	private BigDecimal canceledAmount;

	@Builder.Default
	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentDetail> paymentDetails = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentMetadata> metadataList = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentCancellationDetail> cancellationDetails = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PaymentManipulationDetail> manipulationDetails = new ArrayList<>();

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
			.installmentMonths(0) // Todo: 토스 응답에서 할부 개월 구한 뒤  설정
			.totalPrice(amount)
			.totalFee(fee)
			.paymentDate(LocalDateTime.now())
			.status(PaymentStatus.PAID)
			.build();
	}

	/**
	 * 계약 정보로부터 실패한 결제 정보를 생성하는 정적 팩토리 메서드
	 *
	 * @param contract 계약 정보
	 * @return 생성된 Payment 엔티티
	 */
	public static Payment createFailedPaymentFromContract(Contract contract, BigDecimal fee) {
		return Payment.builder()
			.member(contract.getClient())
			.paymentType(PaymentType.PROJECT)
			.referenceId(contract.getId())
			.itemsSummary(contract.getProject().getTitle())
			.itemsCount(1)
			.installmentMonths(0)
			.totalPrice(contract.getPrice())
			.totalFee(fee)
			.paymentDate(LocalDateTime.now())
			.status(PaymentStatus.FAILED)
			.build();
	}

	/**
	 * 결제 금액 조작이 감지되었을 때 생성하는 정적 팩토리 메서드
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @return 생성된 Payment 엔티티
	 */
	public static Payment createForAmountManipulation(PaymentType paymentType, Long referenceId, Member member,
		String title) {
		return Payment.builder()
			.member(member)
			.paymentType(paymentType)
			.referenceId(referenceId)
			.itemsSummary(title)
			.itemsCount(0)
			.installmentMonths(0)
			.totalPrice(BigDecimal.ZERO)
			.totalFee(BigDecimal.ZERO)
			.paymentDate(LocalDateTime.now())
			.status(PaymentStatus.AMOUNT_MANIPULATED)
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
	 * 메타데이터를 추가합니다.
	 */
	public void addMetadata(PaymentMetadata metadata) {
		this.metadataList.add(metadata);
		metadata.setPayment(this);
	}

	/**
	 * 결제 메타데이터를 가져옵니다.
	 */
	public PaymentMetadata getPaymentMetadata() {
		return this.metadataList.stream()
			.filter(m -> PaymentMetadata.MetadataType.PAYMENT.equals(m.getType()))
			.findFirst()
			.orElse(null);
	}

	/**
	 * 취소 메타데이터 목록을 가져옵니다.
	 */
	public List<PaymentMetadata> getCancellationMetadataList() {
		return this.metadataList.stream()
			.filter(m -> PaymentMetadata.MetadataType.CANCELLATION.equals(m.getType()))
			.collect(Collectors.toList());
	}

	/**
	 * 취소 상세 정보를 추가합니다.
	 */
	public void addCancellationDetail(PaymentCancellationDetail cancellationDetail) {
		this.cancellationDetails.add(cancellationDetail);
		cancellationDetail.setPayment(this);
	}

	/**
	 * 결제 취소 상태를 업데이트합니다.
	 *
	 * @param cancelAmount 취소 금액
	 * @param isFullCancellation 전액 취소 여부
	 */
	public void updateCancellationStatus(BigDecimal cancelAmount, boolean isFullCancellation) {
		// 취소 금액 업데이트 (누적)
		if (this.canceledAmount == null) {
			this.canceledAmount = cancelAmount;
		} else {
			this.canceledAmount = this.canceledAmount.add(cancelAmount);
		}

		// 상태 업데이트
		if (isFullCancellation) {
			this.status = PaymentStatus.FULLY_CANCELED;
		} else {
			this.status = PaymentStatus.PARTIALLY_CANCELED;
		}
	}

	/**
	 * 남은 결제 금액을 계산합니다.
	 *
	 * @return 남은 결제 금액
	 */
	public BigDecimal getRemainingAmount() {
		if (this.canceledAmount == null) {
			return this.totalPrice;
		}
		return this.totalPrice.subtract(this.canceledAmount);
	}

	/**
	 * 결제가 취소되었는지 확인합니다.
	 *
	 * @return 취소 여부
	 */
	public boolean isCanceled() {
		return this.status == PaymentStatus.FULLY_CANCELED;
	}

	/**
	 * 결제가 부분 취소되었는지 확인합니다.
	 *
	 * @return 부분 취소 여부
	 */
	public boolean isPartiallyCanceled() {
		return this.status == PaymentStatus.PARTIALLY_CANCELED;
	}

	/**
	 * 결제 조작 세부 정보를 추가합니다.
	 */
	public void addManipulationDetail(PaymentManipulationDetail manipulationDetail) {
		this.manipulationDetails.add(manipulationDetail);
		manipulationDetail.setPayment(this);
	}

	/**
	 * 결제 상태를 AMOUNT_MANIPULATED로 변경합니다.
	 */
	public void markAsManipulated() {
		this.status = PaymentStatus.AMOUNT_MANIPULATED;
	}
}

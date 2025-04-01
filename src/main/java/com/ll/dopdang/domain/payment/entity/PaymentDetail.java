package com.ll.dopdang.domain.payment.entity;

import java.math.BigDecimal;

import com.ll.dopdang.domain.project.entity.Contract;

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

@Entity
@Table(name = "payment_detail")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Column(name = "item_type")
	private String itemType;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "item_name")
	private String itemName;

	private Integer quantity;

	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "unit_total_price", precision = 10, scale = 2)
	private BigDecimal unitTotalPrice;

	@Column(name = "fee_price", precision = 10, scale = 2)
	private BigDecimal feePrice;

	/**
	 * 계약 정보로부터 결제 상세 정보를 생성하는 정적 팩토리 메서드
	 *
	 * @param payment 결제 정보
	 * @param contract 계약 정보
	 * @param amount 결제 금액
	 * @param fee 수수료
	 * @return 생성된 PaymentDetail 엔티티
	 */
	public static PaymentDetail createFromContract(Payment payment, Contract contract, BigDecimal amount,
		BigDecimal fee) {
		PaymentDetail detail = PaymentDetail.builder()
			.itemType("service")
			.itemId(contract.getId())
			.itemName(contract.getProject().getTitle())
			.quantity(1)
			.unitPrice(amount)
			.unitTotalPrice(amount)
			.feePrice(fee)
			.build();

		payment.addPaymentDetail(detail);
		return detail;
	}

	/**
	 * 결제 정보를 설정합니다.
	 * Payment 엔티티와의 양방향 관계를 위해 사용됩니다.
	 */
	void setPayment(Payment payment) {
		this.payment = payment;
	}

	/**
	 * 수량을 업데이트하고 총액을 재계산합니다.
	 */
	public void updateQuantity(Integer quantity) {
		this.quantity = quantity;
		calculateUnitTotalPrice();
	}

	/**
	 * 단가를 업데이트하고 총액을 재계산합니다.
	 */
	public void updateUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
		calculateUnitTotalPrice();
	}

	/**
	 * 총액을 계산합니다.
	 */
	private void calculateUnitTotalPrice() {
		if (this.quantity != null && this.unitPrice != null) {
			this.unitTotalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
		}
	}
}

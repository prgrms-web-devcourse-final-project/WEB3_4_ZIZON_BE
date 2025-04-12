package com.ll.dopdang.domain.store.entity;

import java.math.BigDecimal;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.global.entity.BaseEntity;

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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@JoinColumn(name = "member_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@NotNull
	@Column(name = "order_number")
	private String orderNumber;

	@NotNull
	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@NotNull
	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@NotNull
	@Column(name = "payment_method")
	private String paymentMethod;

	public static Order from(Member member, String orderNumber, BigDecimal totalAmount, OrderStatus status, String paymentMethod) {
		return Order.builder()
			.member(member)
			.orderNumber(orderNumber)
			.totalAmount(totalAmount)
			.status(status)
			.paymentMethod(paymentMethod)
			.build();
	}
}

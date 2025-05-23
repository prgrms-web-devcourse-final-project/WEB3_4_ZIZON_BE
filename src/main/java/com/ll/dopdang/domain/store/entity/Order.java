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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(name = "order_number", nullable = false)
	private String orderId;

	@Column(name = "total_amount", nullable = false)
	private BigDecimal totalAmount;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod;

	public static Order from(Member member, String orderNumber, BigDecimal totalAmount, OrderStatus status,
		String paymentMethod) {
		return Order.builder()
			.member(member)
			.orderId(orderNumber)
			.totalAmount(totalAmount)
			.status(status)
			.paymentMethod(paymentMethod)
			.build();
	}

	public static Order createOrder(Member member, String orderId, BigDecimal totalAmount) {
		return Order.builder()
			.member(member)
			.orderId(orderId)
			.totalAmount(totalAmount)
			.status(OrderStatus.PAID)
			.paymentMethod("CARD")
			.build();
	}
}

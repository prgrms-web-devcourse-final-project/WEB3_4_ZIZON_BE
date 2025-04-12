package com.ll.dopdang.domain.store.entity;

import java.math.BigDecimal;

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
 * 주문 아이템 정보를 저장하는 엔티티
 */
@Entity
@Table(name = "order_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false)
	private BigDecimal unitPrice;

	@Column(name = "total_price", nullable = false)
	private BigDecimal totalPrice;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	/**
	 * 주문 아이템 생성 정적 팩토리 메서드
	 */
	public static OrderItem createOrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
		return OrderItem.builder()
			.order(order)
			.product(product)
			.quantity(quantity)
			.unitPrice(unitPrice)
			.totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
			.status(OrderStatus.PAID)
			.build();
	}
}
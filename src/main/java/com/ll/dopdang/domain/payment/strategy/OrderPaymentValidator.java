package com.ll.dopdang.domain.payment.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 주문 결제 금액 검증 전략 구현
 */
@Slf4j
@Component
public class OrderPaymentValidator implements PaymentAmountValidator {

	// TODO: OrderService 주입 필요
	// private final OrderService orderService;

	@Override
	public BigDecimal validateAndGetExpectedAmount(Long referenceId, BigDecimal requestAmount) {
		// TODO: 주문에 대한 금액 검증 로직 추가 필요
		// Order order = orderService.getOrderById(referenceId);
		// BigDecimal expectedAmount = order.getTotalPrice();

		// 임시 처리: 요청 금액을 그대로 반환
		log.debug("주문 결제 금액 검증 (임시 처리): 요청 금액={}", requestAmount);
		return requestAmount;
	}
}

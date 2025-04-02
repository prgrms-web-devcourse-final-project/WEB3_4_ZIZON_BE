package com.ll.dopdang.domain.payment.strategy.info;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 결제 정보 제공 전략 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentInfoProvider implements PaymentOrderInfoProvider {

	// TODO: OrderService 주입 필요
	// private final OrderService orderService;

	@Override
	public Map<String, Object> provideAdditionalInfo(Long referenceId) {
		log.debug("주문 결제 정보 조회: referenceId={}", referenceId);

		// TODO: 주문에 대한 정보 추가 로직 필요
		// Order order = orderService.getOrderById(referenceId);
		//
		Map<String, Object> additionalInfo = new HashMap<>();
		//
		// // 주문 정보 추가
		// additionalInfo.put("orderId", order.getId());
		// additionalInfo.put("orderNumber", order.getOrderNumber());
		// additionalInfo.put("totalAmount", order.getTotalAmount());
		// additionalInfo.put("orderDate", order.getOrderDate());
		// additionalInfo.put("itemCount", order.getOrderItems().size());

		return additionalInfo;
	}
}

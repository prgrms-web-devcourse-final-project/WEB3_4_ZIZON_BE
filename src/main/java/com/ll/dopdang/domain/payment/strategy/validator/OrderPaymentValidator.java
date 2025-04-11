package com.ll.dopdang.domain.payment.strategy.validator;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.service.PaymentQueryService;
import com.ll.dopdang.domain.store.dto.ProductDetailResponse;
import com.ll.dopdang.domain.store.service.ProductService;
import com.ll.dopdang.global.exception.PaymentAmountManipulationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 결제 금액 검증 전략 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentValidator implements PaymentAmountValidator {

	private final ProductService productService;
	private final PaymentQueryService paymentQueryService;

	@Override
	public BigDecimal validateAndGetExpectedAmount(Long referenceId, BigDecimal requestAmount, String orderId) {
		// referenceId는 상품 ID
		ProductDetailResponse product = productService.getProductById(referenceId);
		BigDecimal productPrice = product.getPrice();

		// Redis에서 주문 정보 조회하여 수량 가져오기
		PaymentOrderInfo orderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
		Integer quantity = orderInfo.getQuantity();

		// 상품 가격과 수량을 곱한 금액 계산
		BigDecimal expectedAmount = productPrice.multiply(BigDecimal.valueOf(quantity));

		// 예상 금액과 요청 금액 비교
		if (expectedAmount.compareTo(requestAmount) != 0) {
			log.error("결제 금액 불일치: 예상 금액={}, 요청 금액={}", expectedAmount, requestAmount);
			throw new PaymentAmountManipulationException(
				referenceId, expectedAmount, requestAmount, orderId);
		}

		log.debug("주문 결제 금액 검증 성공: 예상 금액={}, 요청 금액={}", expectedAmount, requestAmount);
		return expectedAmount;
	}
}

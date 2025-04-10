package com.ll.dopdang.domain.payment.strategy.info;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.service.PaymentQueryService;
import com.ll.dopdang.domain.store.dto.ProductDetailResponse;
import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.service.ProductService;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 결제 정보 제공 전략 구현
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentInfoProvider implements PaymentOrderInfoProvider {

	private final ProductService productService;
	private final PaymentQueryService paymentQueryService;

	/**
	 * 주문 ID를 사용하여 추가 정보를 제공합니다.
	 *
	 * @param referenceId 참조 ID (상품 ID)
	 * @param orderId 주문 ID
	 * @return 결제 유형에 따른 추가 정보
	 */
	@Override
	public Map<String, Object> provideAdditionalInfo(Long referenceId, String orderId) {
		log.debug("주문 결제 정보 조회: referenceId={}, orderId={}", referenceId, orderId);

		// referenceId는 상품 ID
		ProductDetailResponse product = productService.getProductById(referenceId);

		// 주문 정보 조회
		PaymentOrderInfo orderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);

		Map<String, Object> additionalInfo = new HashMap<>();
		BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(orderInfo.getQuantity()));

		// 상품 정보 추가
		additionalInfo.put("title", product.getTitle());
		additionalInfo.put("price", product.getPrice());
		additionalInfo.put("totalPrice", totalPrice);
		additionalInfo.put("sellerName", product.getExpertName());
		additionalInfo.put("clientId", orderInfo.getMemberId());

		return additionalInfo;
	}

	@Override
	public PaymentResultResponse enrichPaymentResult(Payment payment, PaymentResultResponse baseResponse) {
		// 주문 결제에는 전문가 정보가 없으므로 기본 응답 반환
		return baseResponse;
	}

	/**
	 * 상품의 재고가 주문 수량보다 충분한지 검증합니다.
	 *
	 * @param productId 상품 ID
	 * @param quantity 주문 수량
	 * @throws ServiceException 재고가 부족한 경우
	 */
	public void validateStock(Long productId, Integer quantity) {
		Product product = productService.findById(productId);
		if (product.getStock() < quantity) {
			throw new ServiceException(ErrorCode.INSUFFICIENT_STOCK,
				String.format("상품 재고가 부족합니다. 요청: %d, 재고: %d", quantity, product.getStock()));
		}
		log.info("상품 재고 검증 완료: productId={}, quantity={}, stock={}",
			productId, quantity, product.getStock());
	}
}

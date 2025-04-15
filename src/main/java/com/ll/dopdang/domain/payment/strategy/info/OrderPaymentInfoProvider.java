package com.ll.dopdang.domain.payment.strategy.info;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
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
	private final PaymentRepository paymentRepository; // 추가: PaymentRepository 의존성

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

		Map<String, Object> additionalInfo = new HashMap<>();
		try {
			// referenceId는 상품 ID
			ProductDetailResponse productDetail = productService.getProductById(referenceId);
			Product product = productService.findById(productDetail.id());

			// 주문 정보 조회 - 먼저 Redis에서 시도
			PaymentOrderInfo orderInfo;
			try {
				// Redis에서 정보 조회 시도
				orderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
			} catch (ServiceException redisException) {
				// Redis에서 조회 실패 시 DB에서 직접 조회
				log.info("Redis에서 결제 정보 조회 실패, DB에서 조회 시도: {}", redisException.getMessage());
				Payment payment = paymentRepository.findByOrderId(orderId)
					.orElseThrow(() -> new ServiceException(ErrorCode.ORDER_NOT_FOUND,
						"주문 ID에 해당하는 결제 정보를 찾을 수 없습니다: " + orderId));

				// Payment 엔티티에서 필요한 정보 추출
				orderInfo = new PaymentOrderInfo(
					payment.getPaymentType(),
					payment.getReferenceId(),
					payment.getOrderId(),
					payment.getItemsCount(), // 주문 수량으로 사용
					payment.getMember().getId() // 회원 ID
				);
			}

			BigDecimal totalPrice = productDetail.price().multiply(BigDecimal.valueOf(orderInfo.quantity()));

			// 상품 정보 추가
			additionalInfo.put("title", productDetail.title());
			additionalInfo.put("price", productDetail.price());
			additionalInfo.put("totalPrice", totalPrice);
			additionalInfo.put("sellerName", productDetail.expertName());
			additionalInfo.put("clientId", orderInfo.memberId());
			additionalInfo.put("expertId", product.getExpert().getId());
		} catch (ServiceException exception) {
			log.info("결제 정보 조회 실패: {}", exception.getMessage());
		}

		return additionalInfo;
	}

	@Override
	public PaymentResultResponse enrichPaymentResult(Payment payment, PaymentResultResponse baseResponse) {
		Product product = productService.findById(payment.getReferenceId());

		return baseResponse.withExpertAndPaymentNames(
			product.getExpert().getMember().getName(),
			payment.getItemsSummary());
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

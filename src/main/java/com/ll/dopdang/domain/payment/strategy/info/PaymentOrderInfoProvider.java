package com.ll.dopdang.domain.payment.strategy.info;

import java.util.Map;

import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.global.exception.ServiceException;

public interface PaymentOrderInfoProvider {

	/**
	 * 결제 유형별 추가 정보를 제공합니다.
	 *
	 * @param referenceId 참조 ID
	 * @param orderId 주문 ID
	 * @return 결제 유형에 따른 추가 정보
	 */
	Map<String, Object> provideAdditionalInfo(Long referenceId, String orderId);

	/**
	 * 결제 정보를 기반으로 결제 결과 응답을 생성합니다.
	 *
	 * @param payment 결제 정보
	 * @param baseResponse 기본 응답 객체
	 * @return 추가 정보가 포함된 결제 결과 응답
	 */
	PaymentResultResponse enrichPaymentResult(Payment payment, PaymentResultResponse baseResponse);

	/**
	 * 상품의 재고가 주문 수량보다 충분한지 검증합니다.
	 * 기본 구현은 아무 검증도 수행하지 않습니다.
	 *
	 * @param productId 상품 ID
	 * @param quantity 주문 수량
	 * @throws ServiceException 재고가 부족한 경우
	 */
	default void validateStock(Long productId, Integer quantity) {
		// 기본 구현은 아무 검증도 수행하지 않음
	}
}

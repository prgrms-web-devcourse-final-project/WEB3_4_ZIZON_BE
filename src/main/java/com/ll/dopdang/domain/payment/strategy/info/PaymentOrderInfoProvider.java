package com.ll.dopdang.domain.payment.strategy.info;

import java.util.Map;

/**
 * 결제 유형별 주문 정보를 제공하는 전략 인터페이스
 */
public interface PaymentOrderInfoProvider {

	/**
	 * 결제 유형별 추가 정보를 제공합니다.
	 *
	 * @param referenceId 참조 ID
	 * @return 결제 유형에 따른 추가 정보
	 */
	Map<String, Object> provideAdditionalInfo(Long referenceId);
}

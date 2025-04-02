package com.ll.dopdang.domain.payment.strategy.info;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 기타 결제 정보 제공 전략 구현
 */
@Slf4j
@Component
public class EtcPaymentInfoProvider implements PaymentOrderInfoProvider {

	@Override
	public Map<String, Object> provideAdditionalInfo(Long referenceId) {
		log.debug("기타 결제 정보 조회: referenceId={}", referenceId);

		// 기타 결제의 경우 추가 정보가 없거나 다른 방식으로 처리할 수 있음
		return new HashMap<>();
	}
}

package com.ll.dopdang.domain.payment.strategy.info;

import java.util.Map;

import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;

public interface PaymentOrderInfoProvider {

	/**
	 * 결제 유형별 추가 정보를 제공합니다.
	 *
	 * @param referenceId 참조 ID
	 * @return 결제 유형에 따른 추가 정보
	 */
	Map<String, Object> provideAdditionalInfo(Long referenceId);

	/**
	 * 결제 정보를 기반으로 결제 결과 응답을 생성합니다.
	 *
	 * @param payment 결제 정보
	 * @param baseResponse 기본 응답 객체
	 * @return 추가 정보가 포함된 결제 결과 응답
	 */
	PaymentResultResponse enrichPaymentResult(Payment payment, PaymentResultResponse baseResponse);
}

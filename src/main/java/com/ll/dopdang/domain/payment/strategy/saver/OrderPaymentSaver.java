package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

/**
 * 주문 결제 정보 저장 전략 구현
 */
@Component
public class OrderPaymentSaver implements PaymentSaver {

	@Override
	public Payment savePayment(Long referenceId, BigDecimal amount, BigDecimal fee, String paymentKey) {
		// Todo: 스토어 주문에 대한 저장 로직 추가 필요
		throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "주문 결제 처리 기능이 아직 구현되지 않았습니다.");
	}
}

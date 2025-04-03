package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

/**
 * 기타 결제 정보 저장 전략 구현
 */
@Component
public class EtcPaymentSaver implements PaymentSaver {

	@Override
	public Payment savePayment(Long referenceId, BigDecimal amount, BigDecimal fee, String paymentKey) {
		// Todo: 기타 결제 유형에 대한 결제 성공 데이터 저장 로직 추가 필요
		throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "기타 결제 처리 기능이 아직 구현되지 않았습니다.");
	}

	@Override
	public Payment saveFailedPayment(Long referenceId, String errorCode, String errorMessage) {
		// Todo: 기타 결제 유형에 대한 결제 실패 데이터 저장 로직 추가 필요
		throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "주문 결제 처리 기능이 아직 구현되지 않았습니다.");
	}
}

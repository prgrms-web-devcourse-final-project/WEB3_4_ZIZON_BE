package com.ll.dopdang.domain.payment.strategy.saver;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 결제 유형에 따른 저장 전략을 제공하는 팩토리
 */
@Component
@RequiredArgsConstructor
public class PaymentSaverFactory {

	private final ProjectPaymentSaver projectPaymentSaver;
	private final OrderPaymentSaver orderPaymentSaver;
	private final EtcPaymentSaver etcPaymentSaver;

	private final Map<PaymentType, PaymentSaver> savers = new EnumMap<>(PaymentType.class);

	@PostConstruct
	public void init() {
		savers.put(PaymentType.PROJECT, projectPaymentSaver);
		savers.put(PaymentType.ORDER, orderPaymentSaver);
		savers.put(PaymentType.ETC, etcPaymentSaver);
	}

	/**
	 * 결제 유형에 맞는 저장 전략을 반환합니다.
	 *
	 * @param paymentType 결제 유형
	 * @return 저장 전략
	 */
	public PaymentSaver getSaver(PaymentType paymentType) {
		PaymentSaver saver = savers.get(paymentType);

		if (saver == null) {
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "지원하지 않는 결제 유형입니다.");
		}

		return saver;
	}
}

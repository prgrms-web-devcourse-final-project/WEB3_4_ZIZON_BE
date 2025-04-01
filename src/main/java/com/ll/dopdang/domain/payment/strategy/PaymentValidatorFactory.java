package com.ll.dopdang.domain.payment.strategy;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 결제 유형에 따른 검증 전략을 제공하는 팩토리
 */
@Component
@RequiredArgsConstructor
public class PaymentValidatorFactory {

	private final ProjectPaymentValidator projectPaymentValidator;
	private final OrderPaymentValidator orderPaymentValidator;
	private final EtcPaymentValidator etcPaymentValidator;

	private final Map<PaymentType, PaymentAmountValidator> validators = new EnumMap<>(PaymentType.class);

	@PostConstruct
	public void init() {
		validators.put(PaymentType.PROJECT, projectPaymentValidator);
		validators.put(PaymentType.ORDER, orderPaymentValidator);
		validators.put(PaymentType.ETC, etcPaymentValidator);
	}

	/**
	 * 결제 유형에 맞는 검증 전략을 반환합니다.
	 *
	 * @param paymentType 결제 유형
	 * @return 검증 전략
	 */
	public PaymentAmountValidator getValidator(PaymentType paymentType) {
		PaymentAmountValidator validator = validators.get(paymentType);

		if (validator == null) {
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "지원하지 않는 결제 유형입니다.");
		}

		return validator;
	}
}

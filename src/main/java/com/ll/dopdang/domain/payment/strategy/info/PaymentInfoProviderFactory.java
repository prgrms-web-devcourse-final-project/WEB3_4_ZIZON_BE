package com.ll.dopdang.domain.payment.strategy.info;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * 결제 유형별 정보 제공자를 관리하는 팩토리
 */
@Component
@RequiredArgsConstructor
public class PaymentInfoProviderFactory {

	private final ProjectPaymentInfoProvider projectPaymentInfoProvider;
	private final OrderPaymentInfoProvider orderPaymentInfoProvider;
	private final EtcPaymentInfoProvider etcPaymentInfoProvider;

	private final Map<PaymentType, PaymentOrderInfoProvider> providers = new EnumMap<>(PaymentType.class);

	@PostConstruct
	public void init() {
		providers.put(PaymentType.PROJECT, projectPaymentInfoProvider);
		providers.put(PaymentType.ORDER, orderPaymentInfoProvider);
		providers.put(PaymentType.ETC, etcPaymentInfoProvider);
	}

	/**
	 * 결제 유형에 맞는 정보 제공자를 반환합니다.
	 *
	 * @param paymentType 결제 유형
	 * @return 정보 제공자
	 */
	public PaymentOrderInfoProvider getProvider(PaymentType paymentType) {
		PaymentOrderInfoProvider provider = providers.get(paymentType);

		if (provider == null) {
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "지원하지 않는 결제 유형입니다.");
		}

		return provider;
	}
}

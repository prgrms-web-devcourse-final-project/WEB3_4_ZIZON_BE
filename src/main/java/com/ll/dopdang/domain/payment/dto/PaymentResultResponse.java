package com.ll.dopdang.domain.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResultResponse {
	private String status;
	private BigDecimal amount;
	private String errorCode;
	private String message;
	private String expertName;
	private String paymentName;

	public static PaymentResultResponse success(BigDecimal amount) {
		return PaymentResultResponse.builder()
			.status("success")
			.amount(amount)
			.build();
	}

	public static PaymentResultResponse fail(String errorCode, String message) {
		return PaymentResultResponse.builder()
			.status("fail")
			.errorCode(errorCode)
			.message(message)
			.build();
	}
}

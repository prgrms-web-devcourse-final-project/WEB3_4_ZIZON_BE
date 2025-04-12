package com.ll.dopdang.domain.payment.dto;

import java.math.BigDecimal;

public record PaymentResultResponse(
	String status,
	BigDecimal amount,
	String errorCode,
	String message,
	String expertName,
	String paymentName
) {
	public static PaymentResultResponse success(BigDecimal amount) {
		return new PaymentResultResponse("success", amount, null, null, null, null);
	}

	public static PaymentResultResponse fail(BigDecimal amount, String errorCode, String message) {
		return new PaymentResultResponse("fail", amount, errorCode, message, null, null);
	}

	// Method to create a new instance with updated fields
	public PaymentResultResponse withExpertAndPaymentNames(String expertName, String paymentName) {
		return new PaymentResultResponse(status, amount, errorCode, message, expertName, paymentName);
	}
}

package com.ll.dopdang.domain.payment.util;

/**
 * 결제 관련 상수를 정의하는 클래스
 */
public class PaymentConstants {

	// Redis 키 접두사
	public static final String KEY_PREFIX = "payment:order:";

	// 주문 만료 시간 (분)
	public static final long ORDER_EXPIRY_MINUTES = 10L;

	// 세션 키
	public static final String PAYMENT_RESULT_SESSION_KEY = "paymentResult";

	// 생성자를 private으로 선언하여 인스턴스화 방지
	private PaymentConstants() {
		throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
	}
}

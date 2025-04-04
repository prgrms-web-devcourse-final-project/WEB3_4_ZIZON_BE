package com.ll.dopdang.domain.payment.util;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.ll.dopdang.domain.payment.client.TossPaymentClient;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;

/**
 * 토스페이먼츠 API 호출과 관련된 유틸리티 메서드를 제공하는 클래스
 */
@Slf4j
public class TossPaymentUtils {

	/**
	 * 토스페이먼츠 결제 승인 API를 호출합니다.
	 *
	 * @param tossPaymentClient 토스페이먼츠 API 클라이언트
	 * @param secretKey 토스페이먼츠 시크릿 키
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @return API 응답 데이터
	 */
	public static String confirmPayment(
		TossPaymentClient tossPaymentClient,
		String secretKey,
		String paymentKey,
		String orderId,
		BigDecimal amount) {

		try {
			// Basic 인증 헤더 생성
			String authorization = createAuthorizationHeader(secretKey);

			// 요청 본문 생성
			Map<String, Object> requestBody = Map.of(
				"paymentKey", paymentKey,
				"orderId", orderId,
				"amount", amount
			);

			// API 호출
			ResponseEntity<String> response = tossPaymentClient.confirmPayment(
				authorization,
				requestBody
			);

			// 응답 처리
			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			} else {
				log.error("결제 승인 실패: {}", response.getBody());
				throw new ServiceException(ErrorCode.PAYMENT_CONFIRMATION_FAILED,
					"결제 승인 실패: " + response.getBody());
			}
		} catch (Exception e) {
			log.error("토스페이먼츠 API 호출 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 처리 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 토스페이먼츠 결제 취소 API를 호출합니다.
	 *
	 * @param tossPaymentClient 토스페이먼츠 API 클라이언트
	 * @param secretKey 토스페이먼츠 시크릿 키
	 * @param paymentKey 결제 키
	 * @param cancelReason 취소 사유
	 * @param cancelAmount 취소 금액 (부분 취소 시에만 사용, 전액 취소 시 null)
	 * @return API 응답 데이터
	 */
	public static String cancelPayment(
		TossPaymentClient tossPaymentClient,
		String secretKey,
		String paymentKey,
		String cancelReason,
		BigDecimal cancelAmount) {

		try {
			// Basic 인증 헤더 생성
			String authorization = createAuthorizationHeader(secretKey);

			// 요청 본문 생성
			Map<String, Object> requestBody;
			if (cancelAmount != null) {
				// 부분 취소
				requestBody = Map.of(
					"cancelReason", cancelReason,
					"cancelAmount", cancelAmount
				);
			} else {
				// 전액 취소
				requestBody = Map.of(
					"cancelReason", cancelReason
				);
			}

			// API 호출
			ResponseEntity<String> response = tossPaymentClient.cancelPayment(
				authorization,
				paymentKey,
				requestBody
			);

			// 응답 처리
			if (response.getStatusCode().is2xxSuccessful()) {
				return response.getBody();
			} else {
				log.error("결제 취소 실패: {}", response.getBody());
				throw new ServiceException(ErrorCode.PAYMENT_CANCELLATION_FAILED,
					"결제 취소 실패: " + response.getBody());
			}
		} catch (Exception e) {
			log.error("토스페이먼츠 취소 API 호출 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 취소 중 오류가 발생했습니다.");
		}
	}

	/**
	 * Basic 인증 헤더를 생성합니다.
	 *
	 * @param secretKey 토스페이먼츠 시크릿 키
	 * @return Basic 인증 헤더 값
	 */
	private static String createAuthorizationHeader(String secretKey) {
		String authString = secretKey + ":";
		String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
		return "Basic " + encodedAuthString;
	}
}

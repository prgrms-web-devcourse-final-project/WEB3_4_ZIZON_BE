package com.ll.dopdang.domain.payment.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossPaymentClient", url = "${payment.toss.api-url}")
public interface TossPaymentClient {

	/**
	 * 결제 승인 API를 호출합니다.
	 *
	 * @param authorization 인증 헤더
	 * @param requestBody 요청 본문
	 * @return API 응답
	 */
	@PostMapping(value = "/v1/payments/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> confirmPayment(
		@RequestHeader("Authorization") String authorization,
		@RequestBody Map<String, Object> requestBody
	);

	/**
	 * 결제 취소 API를 호출합니다.
	 *
	 * @param authorization 인증 헤더
	 * @param paymentKey 결제 키
	 * @param requestBody 취소 요청 본문
	 * @return API 응답
	 */
	@PostMapping(value = "/v1/payments/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> cancelPayment(
		@RequestHeader("Authorization") String authorization,
		@PathVariable("paymentKey") String paymentKey,
		@RequestBody Map<String, Object> requestBody);
}

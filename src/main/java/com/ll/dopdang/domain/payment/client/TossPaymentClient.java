package com.ll.dopdang.domain.payment.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossPaymentClient", url = "${payment.toss.api-url}")
public interface TossPaymentClient {

	@PostMapping(value = "/v1/payments/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> confirmPayment(
		@RequestHeader("Authorization") String authorization,
		@RequestBody Map<String, Object> requestBody
	);
}

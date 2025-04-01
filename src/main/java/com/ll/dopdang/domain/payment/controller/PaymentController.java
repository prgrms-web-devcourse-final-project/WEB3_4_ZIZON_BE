package com.ll.dopdang.domain.payment.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.payment.dto.OrderIdRequest;
import com.ll.dopdang.domain.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;
	private final MemberService memberService;

	/**
	 * 결제를 위한 주문 ID를 생성합니다.
	 *
	 * @param request 결제 유형과 참조 ID가 포함된 요청 객체
	 * @return 주문 ID와 고객 키가 포함된 응답
	 */
	@PostMapping("/orderId")
	public ResponseEntity<?> createOrderId(@RequestBody @Valid OrderIdRequest request) {

		log.info("주문 ID 생성 요청: paymentType={}, referenceId={}", request.getPaymentType(), request.getReferenceId());

		String orderId = paymentService.createOrderId(request.getPaymentType(), request.getReferenceId());

		//Todo: jwt 연동 시 인증된 사용자 정보로 멤버 찾도록 수정
		Member member = memberService.getMemberById(1L);
		String customerKey = member.getUniqueKey();

		Map<String, Object> response = new HashMap<>();
		response.put("orderId", orderId);
		response.put("customerKey", customerKey);

		return ResponseEntity.ok(response);
	}

	/**
	 * 토스페이먼츠 결제 성공 콜백을 처리합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @return 결제 성공 응답
	 */
	@GetMapping("/success")
	public ResponseEntity<?> tossPaymentsSuccess(
		@RequestParam String paymentKey,
		@RequestParam String orderId,
		@RequestParam BigDecimal amount) {

		log.info("결제 성공 콜백 호출: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

		paymentService.confirmPayment(paymentKey, orderId, amount);

		Map<String, Object> response = new HashMap<>();
		response.put("paymentKey", paymentKey);
		response.put("orderId", orderId);
		response.put("amount", amount);
		response.put("message", "결제가 성공적으로 완료되었습니다.");

		return ResponseEntity.ok(response);
	}

	/**
	 * 토스페이먼츠 결제 실패 콜백을 처리합니다.
	 *
	 * @param code 오류 코드
	 * @param message 오류 메시지
	 * @param orderId 주문 ID
	 * @return 결제 실패 응답
	 */
	@GetMapping("/fail")
	public ResponseEntity<?> tossPaymentsFail(
		@RequestParam String code,
		@RequestParam String message,
		@RequestParam String orderId) {

		log.error("결제 실패: code={}, message={}, orderId={}", code, message, orderId);

		Map<String, Object> response = new HashMap<>();
		response.put("code", code);
		response.put("message", message);
		response.put("orderId", orderId);

		return ResponseEntity.badRequest().body(response);
	}
}

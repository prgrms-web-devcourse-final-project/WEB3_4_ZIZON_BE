package com.ll.dopdang.domain.payment.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import com.ll.dopdang.domain.payment.dto.PaymentCancellationRequest;
import com.ll.dopdang.domain.payment.dto.PaymentCancellationResponse;
import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.service.PaymentCancellationService;
import com.ll.dopdang.domain.payment.service.PaymentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

	private static final String PAYMENT_RESULT_SESSION_KEY = "paymentResult";
	private final PaymentService paymentService;
	private final PaymentCancellationService paymentCancellationService;
	private final MemberService memberService;

	/**
	 * 결제를 위한 주문 정보를 생성합니다.
	 *
	 * @param request 주문 정보 생성 요청 (결제 유형, 참조 ID 포함)
	 * @return 생성된 주문 ID와 결제에 필요한 추가 정보가 포함된 응답
	 */
	@PostMapping("/orderId")
	public ResponseEntity<?> createPaymentOrderInfo(@RequestBody @Valid OrderIdRequest request) {
		log.info("주문 ID 생성 요청: paymentType={}, referenceId={}", request.getPaymentType(), request.getReferenceId());

		//Todo: jwt 연동 시 인증된 사용자 정보로 멤버 찾도록 수정
		Member member = memberService.getMemberById(1L);

		// 서비스에서 주문 ID 생성 및 추가 정보 조회
		Map<String, Object> response = paymentService.createOrderIdWithInfo(
			request.getPaymentType(),
			request.getReferenceId(),
			member
		);

		return ResponseEntity.ok(response);
	}

	/**
	 * 결제 성공 콜백 처리
	 * 토스페이먼츠에서 결제 성공 시 리다이렉트되는 엔드포인트
	 *
	 * 1. 결제 검증 및 확정 처리
	 * 2. 결제 정보를 기반으로 결제 결과 응답 생성 (전문가 이름 등 추가 정보 포함)
	 * 3. 결제 결과를 세션에 저장
	 * 4. 결과 페이지로 리다이렉트
	 *
	 * @param paymentKey 토스페이먼츠에서 발급한 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @param session HTTP 세션
	 * @return 결과 페이지로 리다이렉트 응답
	 */
	@GetMapping("/success")
	public ResponseEntity<?> tossPaymentsSuccess(
		@RequestParam String paymentKey,
		@RequestParam String orderId,
		@RequestParam BigDecimal amount,
		HttpSession session) throws URISyntaxException {

		log.info("결제 성공 콜백 호출: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

		Payment payment = paymentService.confirmPayment(paymentKey, orderId, amount);
		// 서비스 레이어를 통해 결제 결과 응답 생성 (전문가 이름 등 추가 정보 포함)
		PaymentResultResponse response = paymentService.createPaymentResultResponse(payment, amount);

		session.setAttribute(PAYMENT_RESULT_SESSION_KEY, response);

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI("/payments/result"));

		return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
	}

	/**
	 * 결제 실패 콜백 처리
	 * 토스페이먼츠에서 결제 실패 시 리다이렉트되는 엔드포인트
	 *
	 * 1. 실패 정보 로깅
	 * 2. 결제 실패 결과 응답 생성
	 * 3. 결제 실패 결과를 세션에 저장
	 * 4. 결과 페이지로 리다이렉트
	 *
	 * @param code 실패 코드
	 * @param message 실패 메시지
	 * @param orderId 주문 ID
	 * @param session HTTP 세션
	 * @return 결과 페이지로 리다이렉트 응답
	 */
	@GetMapping("/fail")
	public ResponseEntity<?> tossPaymentsFail(
		@RequestParam String code,
		@RequestParam String message,
		@RequestParam String orderId,
		HttpSession session) throws URISyntaxException {

		log.error("결제 실패: code={}, message={}, orderId={}", code, message, orderId);

		// 실패한 결제 정보 저장
		Payment failedPayment = paymentService.saveFailedPayment(orderId, code, message);
		// 서비스 레이어를 통해 결제 결과 응답 생성 (전문가 이름 등 추가 정보 포함)
		PaymentResultResponse response = paymentService.createFailedPaymentResultResponse(failedPayment, message, code);

		session.setAttribute(PAYMENT_RESULT_SESSION_KEY, response);

		// 리다이렉트 경로는 그대로 유지
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI("/payments/result"));

		return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
	}

	/**
	 * 결제 결과 조회
	 * 결제 성공/실패 후 리다이렉트되는 결과 페이지에서 호출되는 엔드포인트
	 *
	 * 1. 세션에서 결제 결과 조회
	 * 2. 결제 결과가 없으면 400 Bad Request 응답
	 * 3. 결제 결과가 있으면 결과 반환 및 세션에서 제거
	 *
	 * @param session HTTP 세션
	 * @return 결제 결과 응답
	 */
	@GetMapping("/result")
	public ResponseEntity<PaymentResultResponse> paymentResult(HttpSession session) {
		// 세션에서 결제 결과 조회
		PaymentResultResponse result = (PaymentResultResponse)session.getAttribute(PAYMENT_RESULT_SESSION_KEY);

		if (result == null) {
			log.error("결제 결과를 찾을 수 없습니다.");
			return ResponseEntity.badRequest().build();
		}

		// 세션에서 결제 결과 제거 (일회성 데이터)
		session.removeAttribute(PAYMENT_RESULT_SESSION_KEY);

		return ResponseEntity.ok(result);
	}

	/**
	 * 결제를 취소합니다. cancelAmount가 null이면 전액 취소, 값이 있으면 부분 취소로 처리합니다.
	 *
	 * @param request 취소 요청 정보 (결제 유형, 참조 ID, 취소 사유, 취소 금액(선택적) 포함)
	 * @return 취소 결과
	 */
	@PostMapping("/cancel")
	public ResponseEntity<?> cancelPayment(@RequestBody @Valid PaymentCancellationRequest request) {

		log.info("결제 취소 요청: paymentType={}, referenceId={}, reason={}, amount={}",
			request.getPaymentType(), request.getReferenceId(), request.getCancelReason(), request.getCancelAmount());

		Payment payment = paymentCancellationService.cancelPayment(
			request.getPaymentType(),
			request.getReferenceId(),
			request.getCancelReason(),
			request.getCancelAmount()
		);

		return ResponseEntity.ok(PaymentCancellationResponse.from(payment));
	}

}

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "결제", description = "결제 및 결제 취소 API")
public class PaymentController {

	private static final String PAYMENT_RESULT_SESSION_KEY = "paymentResult";

	private final PaymentService paymentService;
	private final PaymentCancellationService paymentCancellationService;
	private final MemberService memberService;

	@Operation(
		summary = "주문 ID 생성",
		description = "결제를 위한 주문 ID 및 결제에 필요한 추가 정보를 생성합니다."
	)
	@ApiResponse(responseCode = "200", description = "주문 ID 생성 성공")
	@ApiResponse(responseCode = "400", description = "요청 파라미터 오류")
	@PostMapping("/orderId")
	public ResponseEntity<?> createPaymentOrderInfo(
		@RequestBody @Valid OrderIdRequest request
	) {
		log.info("주문 ID 생성 요청: paymentType={}, referenceId={}", request.getPaymentType(), request.getReferenceId());
		// TODO: JWT 연동 시 인증 사용자 기반으로 변경
		Member member = memberService.getMemberById(1L);
		Map<String, Object> response = paymentService.createOrderIdWithInfo(
			request.getPaymentType(),
			request.getReferenceId(),
			member
		);
		return ResponseEntity.ok(response);
	}

	@Operation(
		summary = "결제 성공 콜백",
		description = "토스페이먼츠 결제 성공 후 호출되는 콜백입니다. 결제 검증 및 결과 세션 저장 후 결과 페이지로 리다이렉트됩니다."
	)
	@ApiResponse(responseCode = "303", description = "결제 성공, 결과 페이지로 리다이렉트")
	@ApiResponse(responseCode = "400", description = "검증 실패 또는 금액 불일치")
	@GetMapping("/success")
	public ResponseEntity<?> tossPaymentsSuccess(
		@Parameter(description = "토스페이먼츠 결제 키", example = "pay_abc123")
		@RequestParam String paymentKey,
		@Parameter(description = "주문 ID", example = "order_456")
		@RequestParam String orderId,
		@Parameter(description = "결제 금액", example = "50000")
		@RequestParam BigDecimal amount,
		HttpSession session
	) throws URISyntaxException {
		log.info("결제 성공 콜백 호출: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
		Payment payment = paymentService.confirmPayment(paymentKey, orderId, amount);
		PaymentResultResponse response = paymentService.createPaymentResultResponse(payment, amount);
		session.setAttribute(PAYMENT_RESULT_SESSION_KEY, response);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI("/payments/result"));
		return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
	}

	@Operation(
		summary = "결제 실패 콜백",
		description = "토스페이먼츠 결제 실패 시 호출되는 콜백입니다. 실패 정보 기록 후 결과 페이지로 리다이렉트됩니다."
	)

	@ApiResponse(responseCode = "303", description = "결제 실패, 결과 페이지로 리다이렉트")
	@GetMapping("/fail")
	public ResponseEntity<?> tossPaymentsFail(
		@Parameter(description = "실패 코드", example = "USER_CANCEL")
		@RequestParam String code,
		@Parameter(description = "실패 메시지", example = "사용자에 의해 취소되었습니다.")
		@RequestParam String message,
		@Parameter(description = "주문 ID", example = "order_456")
		@RequestParam String orderId,
		HttpSession session
	) throws URISyntaxException {
		log.error("결제 실패: code={}, message={}, orderId={}", code, message, orderId);
		Payment failedPayment = paymentService.saveFailedPayment(orderId, code, message);
		PaymentResultResponse response = paymentService.createFailedPaymentResultResponse(failedPayment, message, code);
		session.setAttribute(PAYMENT_RESULT_SESSION_KEY, response);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(new URI("/payments/result"));
		return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
	}

	@Operation(
		summary = "결제 결과 조회",
		description = "결제 성공/실패 후 리다이렉트된 페이지에서 결제 결과를 조회합니다. 세션에 저장된 결과가 반환됩니다."
	)
	@ApiResponse(responseCode = "200", description = "결제 결과 조회 성공")
	@ApiResponse(responseCode = "400", description = "결제 결과가 존재하지 않음")
	@GetMapping("/result")
	public ResponseEntity<PaymentResultResponse> paymentResult(HttpSession session) {
		PaymentResultResponse result = (PaymentResultResponse)session.getAttribute(PAYMENT_RESULT_SESSION_KEY);
		if (result == null) {
			log.error("결제 결과를 찾을 수 없습니다.");
			return ResponseEntity.badRequest().build();
		}
		session.removeAttribute(PAYMENT_RESULT_SESSION_KEY);
		return ResponseEntity.ok(result);
	}

	@Operation(
		summary = "결제 취소",
		description = "결제를 취소합니다. 금액이 없으면 전체 취소, 값이 있으면 부분 취소됩니다."
	)
	@ApiResponse(responseCode = "200", description = "결제 취소 성공")
	@ApiResponse(responseCode = "400", description = "요청 오류 또는 취소 불가")
	@PostMapping("/cancel")
	public ResponseEntity<?> cancelPayment(
		@Parameter(description = "결제 취소 요청")
		@RequestBody @Valid PaymentCancellationRequest request
	) {
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

package com.ll.dopdang.domain.payment.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.payment.dto.OrderIdRequest;
import com.ll.dopdang.domain.payment.dto.PaymentCancellationRequest;
import com.ll.dopdang.domain.payment.dto.PaymentCancellationResponse;
import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.service.PaymentCancellationService;
import com.ll.dopdang.domain.payment.service.PaymentCreationService;
import com.ll.dopdang.domain.payment.service.PaymentProcessingService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;
import com.ll.dopdang.standard.util.LogSanitizer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
@Tag(name = "결제", description = "결제 및 결제 취소 API")
public class PaymentController {

	private final PaymentCreationService paymentCreationService;
	private final PaymentProcessingService paymentProcessingService;
	private final PaymentCancellationService paymentCancellationService;

	/**
	 * 결제를 위한 주문 ID를 생성합니다.
	 *
	 * @param request 주문 ID 생성 요청 정보 (결제 유형, 참조 ID 포함)
	 * @return 생성된 주문 ID와 결제 관련 정보
	 */
	@Operation(summary = "주문 ID 생성", description = "결제를 위한 주문 ID 및 결제에 필요한 추가 정보를 생성합니다.")
	@ApiResponse(responseCode = "200", description = "주문 ID 생성 성공")
	@ApiResponse(responseCode = "400", description = "요청 파라미터 오류")
	@PostMapping("/orderId")
	public ResponseEntity<?> createPaymentOrderInfo(
		@RequestBody @Valid OrderIdRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		log.info("주문 ID 생성 요청: paymentType={}, referenceId={}, quantity={}",
			LogSanitizer.sanitizeLogInput(request.paymentType().toString()), request.referenceId(), request.quantity());

		Map<String, Object> response = paymentCreationService.createOrderIdWithInfo(
			request.paymentType(), request.referenceId(), userDetails.getMember().getId(), request.quantity());

		return ResponseEntity.ok(response);
	}

	/**
	 * 토스페이먼츠 결제 성공 콜백을 처리합니다.
	 *
	 * @param paymentKey 토스페이먼츠 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @return 결제 결과 정보
	 */
	@Operation(summary = "결제 성공 콜백", description = "토스페이먼츠 결제 성공 후 호출되는 콜백입니다. 결제 검증 및 결과 세션 저장 후 결과 페이지로 리다이렉트됩니다.")
	@ApiResponse(responseCode = "303", description = "결제 성공, 결과 페이지로 리다이렉트")
	@ApiResponse(responseCode = "400", description = "검증 실패 또는 금액 불일치")
	@GetMapping("/success")
	public ResponseEntity<?> tossPaymentsSuccess(
		@Parameter(description = "토스페이먼츠 결제 키", example = "pay_abc123") @RequestParam String paymentKey,
		@Parameter(description = "주문 ID", example = "order_456") @RequestParam String orderId,
		@Parameter(description = "결제 금액", example = "50000") @RequestParam BigDecimal amount) {
		log.info("결제 성공 콜백 호출: paymentKey={}, orderId={}, amount={}", LogSanitizer.sanitizeLogInput(paymentKey),
			orderId, amount);
		Payment payment = paymentProcessingService.confirmPayment(paymentKey, orderId, amount);
		PaymentResultResponse response = paymentProcessingService.createPaymentResultResponse(payment, amount);
		return ResponseEntity.ok(response);
	}

	/**
	 * 토스페이먼츠 결제 실패 콜백을 처리합니다.
	 *
	 * @param code 실패 코드
	 * @param message 실패 메시지
	 * @param orderId 주문 ID
	 * @return 결제 실패 결과 정보
	 */
	@Operation(summary = "결제 실패 콜백", description = "토스페이먼츠 결제 실패 시 호출되는 콜백입니다. 실패 정보 기록 후 결과 페이지로 리다이렉트됩니다.")
	@ApiResponse(responseCode = "303", description = "결제 실패, 결과 페이지로 리다이렉트")
	@GetMapping("/fail")
	public ResponseEntity<?> tossPaymentsFail(
		@Parameter(description = "실패 코드", example = "USER_CANCEL") @RequestParam String code,
		@Parameter(description = "실패 메시지", example = "사용자에 의해 취소되었습니다.") @RequestParam String message,
		@Parameter(description = "주문 ID", example = "order_456") @RequestParam String orderId) {
		log.error("결제 실패: code={}, message={}, orderId={}", LogSanitizer.sanitizeLogInput(code),
			LogSanitizer.sanitizeLogInput(message), orderId);
		Payment failedPayment = paymentProcessingService.saveFailedPayment(orderId, code, message);
		PaymentResultResponse response = paymentProcessingService.createFailedPaymentResultResponse(failedPayment,
			message, code);
		return ResponseEntity.ok(response);
	}

	/**
	 * 결제를 취소합니다. cancelAmount가 null이면 전액 취소, 값이 있으면 부분 취소로 처리합니다.
	 *
	 * @param request 취소 요청 정보 (결제 유형, 참조 ID, 취소 사유, 취소 금액(선택적) 포함)
	 * @return 취소 결과
	 */
	@Operation(summary = "결제 취소", description = "결제를 취소합니다. 금액이 없으면 전체 취소, 값이 있으면 부분 취소됩니다.")
	@ApiResponse(responseCode = "200", description = "결제 취소 성공")
	@ApiResponse(responseCode = "400", description = "요청 오류 또는 취소 불가")
	@PostMapping("/cancel")
	public ResponseEntity<?> cancelPayment(
		@Parameter(description = "결제 취소 요청") @RequestBody @Valid PaymentCancellationRequest request) {
		log.info("결제 취소 요청: orderId={}, reason={}, amount={}",
			request.orderId(), LogSanitizer.sanitizeLogInput(request.cancelReason()), request.cancelAmount());

		Payment payment = paymentCancellationService.cancelPayment(request.orderId(), request.cancelReason(),
			request.cancelAmount());

		return ResponseEntity.ok(PaymentCancellationResponse.from(payment));
	}
}

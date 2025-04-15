package com.ll.dopdang.domain.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.payment.client.TossPaymentClient;
import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentMetadata;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.strategy.info.PaymentInfoProviderFactory;
import com.ll.dopdang.domain.payment.strategy.saver.PaymentSaverFactory;
import com.ll.dopdang.domain.payment.util.PaymentConstants;
import com.ll.dopdang.domain.payment.util.TossPaymentUtils;
import com.ll.dopdang.domain.store.service.ProductService;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.PaymentAmountManipulationException;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.standard.util.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 처리 및 결과 관리 로직을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

	private final RedisRepository redisRepository;
	private final PaymentRepository paymentRepository;
	private final ObjectMapper objectMapper;
	private final TossPaymentClient tossPaymentClient;
	private final PaymentSaverFactory saverFactory;
	private final PaymentVerificationService paymentVerificationService;
	private final PaymentQueryService paymentQueryService;
	private final PaymentInfoProviderFactory paymentInfoProviderFactory;
	private final ProductService productService;

	@Value("${payment.toss.secretKey}")
	private String tossSecretKey;

	@Value("${payment.fee.rate}")
	private BigDecimal feeRate;

	/**
	 * 토스페이먼츠 결제 승인을 처리합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 */
	@Transactional(noRollbackFor = PaymentAmountManipulationException.class)
	public Payment confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
		log.info("결제 승인 요청: paymentKey={}, orderId={}, amount={}", LogSanitizer.sanitizeLogInput(paymentKey), orderId,
			amount);

		// 주문 ID로 결제 정보 조회
		PaymentOrderInfo orderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
		PaymentType paymentType = orderInfo.paymentType();
		Long referenceId = orderInfo.referenceId();

		// 결제 금액 검증
		paymentVerificationService.validatePaymentAmount(paymentType, referenceId, amount, orderId);

		// 상품 재고 감소 처리 - ORDER 타입인 경우에만 수행
		productService.decreaseStock(paymentType, referenceId, orderInfo.quantity());

		// 토스페이먼츠 API 호출
		String responseBody = callTossPaymentsApi(paymentKey, orderId, amount);

		// 결제 정보 저장
		Payment payment = savePaymentInformation(paymentType, referenceId, amount, responseBody, orderId);

		// Redis에서 주문 정보 삭제
		redisRepository.remove(PaymentConstants.KEY_PREFIX + orderId);

		log.info("결제 승인 성공: paymentKey={}", LogSanitizer.sanitizeLogInput(paymentKey));
		return payment;
	}

	/**
	 * 결제 실패 정보를 저장합니다.
	 *
	 * @param orderId 주문 ID
	 * @param errorCode 실패 코드
	 * @param errorMessage 실패 메시지
	 * @return 저장된 Payment 엔티티
	 */
	@Transactional
	public Payment saveFailedPayment(String orderId, String errorCode, String errorMessage) {
		try {
			// 주문 ID로 Redis에서 결제 정보 조회
			PaymentOrderInfo orderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
			PaymentType paymentType = orderInfo.paymentType();
			Long referenceId = orderInfo.referenceId();

			// 실패 메타데이터 생성
			Map<String, String> failureMetadata = new HashMap<>();
			failureMetadata.put("code", errorCode);
			failureMetadata.put("message", errorMessage);
			failureMetadata.put("orderId", orderId);

			String metadataJson;
			try {
				metadataJson = objectMapper.writeValueAsString(failureMetadata);
			} catch (Exception e) {
				log.error("결제 실패 메타데이터 JSON 변환 오류", e);
				metadataJson = String.format("{\"code\":\"%s\",\"message\":\"%s\",\"orderId\":\"%s\"}",
					errorCode, errorMessage, orderId);
			}

			// 전략 패턴을 사용하여 결제 유형에 맞는 결제 실패 데이터 저장 로직 실행
			Payment payment = saverFactory.getSaver(paymentType)
				.saveFailedPayment(referenceId, errorCode, errorMessage, orderId);

			// 실패 메타데이터 추가
			PaymentMetadata.createFailedPaymentMetadata(payment, metadataJson);

			// 결제 정보 저장
			Payment savedPayment = paymentRepository.save(payment);

			// Redis에서 주문 정보 삭제
			redisRepository.remove(PaymentConstants.KEY_PREFIX + orderId);

			log.info("결제 실패 정보 저장 완료: orderId={}, errorCode={}", orderId, LogSanitizer.sanitizeLogInput(errorCode));
			return savedPayment;
		} catch (Exception e) {
			log.error("결제 실패 정보 저장 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 실패 정보 저장 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 토스페이먼츠 결제 승인 API를 호출하여 결제를 승인합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @return API 응답 데이터
	 */
	private String callTossPaymentsApi(String paymentKey, String orderId, BigDecimal amount) {
		return TossPaymentUtils.confirmPayment(
			tossPaymentClient,
			tossSecretKey,
			paymentKey,
			orderId,
			amount
		);
	}

	/**
	 * 결제 정보를 저장합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param amount 결제 금액
	 * @param responseBody 토스페이먼츠 응답 데이터
	 */
	@Transactional
	public Payment savePaymentInformation(PaymentType paymentType, Long referenceId, BigDecimal amount,
		String responseBody, String orderId) {
		try {
			// 토스페이먼츠 응답에서 paymentKey 추출
			Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
			String paymentKey = (String)responseMap.get("paymentKey");

			// 수수료 계산
			BigDecimal fee = calculateFee(amount);

			// 전략 패턴을 사용하여 결제 유형에 맞는 저장 로직 실행
			Payment payment = saverFactory.getSaver(paymentType)
				.savePayment(referenceId, amount, fee, paymentKey, orderId);

			// PaymentMetadata 엔티티 생성
			PaymentMetadata.createPaymentMetadata(payment, responseBody);

			// 결제 정보 저장
			paymentRepository.save(payment);

			log.info("결제 정보 저장 완료: paymentId={} paymentKey={}, amount={}, fee={}",
				payment.getId(), LogSanitizer.sanitizeLogInput(paymentKey), amount, fee);
			return payment;
		} catch (Exception e) {
			log.error("결제 정보 저장 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 정보 저장 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 결제 금액에 대한 수수료를 계산합니다.
	 *
	 * @param amount 결제 금액
	 * @return 계산된 수수료
	 */
	public BigDecimal calculateFee(BigDecimal amount) {
		return amount.multiply(feeRate);
	}

	/**
	 * 결제 정보를 기반으로 결제 결과 응답을 생성합니다.
	 * 결제 유형에 따라 다른 추가 정보를 포함합니다.
	 *
	 * @param payment 결제 정보
	 * @param amount 결제 금액
	 * @return 결제 결과 응답
	 */
	public PaymentResultResponse createPaymentResultResponse(Payment payment, BigDecimal amount) {
		// 기본 결제 결과 응답 생성
		PaymentResultResponse baseResponse = PaymentResultResponse.success(amount);

		// 결제 유형에 맞는 정보 제공자를 통해 응답 보강
		return paymentInfoProviderFactory.getProvider(payment.getPaymentType())
			.enrichPaymentResult(payment, baseResponse);
	}

	/**
	 * 결제 정보를 기반으로 결제 결과 응답을 생성합니다.
	 * 결제 유형에 따라 다른 추가 정보를 포함합니다.
	 *
	 * @param payment 결제 정보
	 * @param message 에러 메시지
	 * @param code 에러 코드
	 * @return 결제 결과 응답
	 */
	public PaymentResultResponse createFailedPaymentResultResponse(Payment payment, String message, String code) {
		// 기본 결제 결과 응답 생성
		PaymentResultResponse baseResponse = PaymentResultResponse.fail(payment.getTotalPrice(), code, message);

		// 결제 유형에 맞는 정보 제공자를 통해 응답 보강
		return paymentInfoProviderFactory.getProvider(payment.getPaymentType())
			.enrichPaymentResult(payment, baseResponse);
	}
}

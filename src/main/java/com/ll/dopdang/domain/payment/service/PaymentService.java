package com.ll.dopdang.domain.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.payment.client.TossPaymentClient;
import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.dto.PaymentResultResponse;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentManipulationDetail;
import com.ll.dopdang.domain.payment.entity.PaymentMetadata;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.strategy.info.PaymentInfoProviderFactory;
import com.ll.dopdang.domain.payment.strategy.saver.PaymentSaverFactory;
import com.ll.dopdang.domain.payment.strategy.validator.PaymentValidatorFactory;
import com.ll.dopdang.domain.payment.util.TossPaymentUtils;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.PaymentAmountManipulationException;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 생성 및 승인 관련 기능을 제공하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private static final String KEY_PREFIX = "payment:order:";
	private static final long ORDER_EXPIRY_MINUTES = 10L;

	private final RedisRepository redisRepository;
	private final PaymentRepository paymentRepository;
	private final ObjectMapper objectMapper;
	private final TossPaymentClient tossPaymentClient;
	private final PaymentValidatorFactory validatorFactory;
	private final PaymentSaverFactory saverFactory;
	private final PaymentInfoProviderFactory infoProviderFactory;

	@Value("${payment.toss.secretKey}")
	private String tossSecretKey;

	@Value("${payment.fee.rate}")
	private BigDecimal feeRate;

	/**
	 * 결제 유형과 참조 ID에 대한 주문 ID를 생성하고 관련 정보를 반환합니다.
	 * 이미 결제가 완료된 경우 ServiceException을 발생시킵니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param member 회원 정보
	 * @return 주문 ID와 고객 키, 결제 유형별 추가 정보가 포함된 맵
	 * @throws ServiceException 이미 결제가 완료된 경우
	 */
	public Map<String, Object> createOrderIdWithInfo(PaymentType paymentType, Long referenceId, Member member) {
		// 이미 성공적으로 결제가 완료된 건인지 확인 (실패한 결제는 제외)
		Optional<Payment> successfulPayment = paymentRepository.findByPaymentTypeAndReferenceIdAndStatus(
			paymentType, referenceId, PaymentStatus.PAID);

		boolean alreadyPaid = successfulPayment.isPresent();

		if (alreadyPaid) {
			log.warn("이미 결제가 완료된 건입니다: paymentType={}, referenceId={}", paymentType, referenceId);
			throw new ServiceException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
		}

		String orderId = UUID.randomUUID().toString();
		String redisKey = KEY_PREFIX + orderId;

		PaymentOrderInfo orderInfo = new PaymentOrderInfo(paymentType, referenceId);
		redisRepository.save(redisKey, orderInfo, ORDER_EXPIRY_MINUTES, TimeUnit.MINUTES);

		log.info("주문 ID 생성 완료: orderId={}, paymentType={}, referenceId={}",
			orderId, paymentType, referenceId);

		// 기본 응답 정보
		Map<String, Object> response = new HashMap<>();
		response.put("orderId", orderId);
		response.put("customerKey", member.getUniqueKey());

		// 결제 유형별 추가 정보 제공
		Map<String, Object> additionalInfo = infoProviderFactory.getProvider(paymentType)
			.provideAdditionalInfo(referenceId);

		// 추가 정보를 응답에 병합
		response.putAll(additionalInfo);

		return response;
	}

	/**
	 * 토스페이먼츠 결제 승인을 처리합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 */
	// 해당 메서드에 @Transactional을 붙일 경우 결제 금액 검증이 실패했을 때 데이터 저장 안됨. 각 메서드에 트랜잭션 붙일 것.
	public Payment confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
		log.info("결제 승인 요청: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

		// 주문 ID로 결제 정보 조회
		PaymentOrderInfo orderInfo = getPaymentOrderInfoByOrderId(orderId);
		PaymentType paymentType = orderInfo.getPaymentType();
		Long referenceId = orderInfo.getReferenceId();

		// 결제 금액 검증
		validatePaymentAmount(paymentType, referenceId, amount);

		// 토스페이먼츠 API 호출
		String responseBody = callTossPaymentsApi(paymentKey, orderId, amount);

		// 결제 정보 저장
		Payment payment = savePaymentInformation(paymentType, referenceId, amount, responseBody);

		// Redis에서 주문 정보 삭제
		redisRepository.remove(KEY_PREFIX + orderId);

		log.info("결제 승인 성공: paymentKey={}", paymentKey);
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
			PaymentOrderInfo orderInfo = getPaymentOrderInfoByOrderId(orderId);
			PaymentType paymentType = orderInfo.getPaymentType();
			Long referenceId = orderInfo.getReferenceId();

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
				.saveFailedPayment(referenceId, errorCode, errorMessage);

			// 실패 메타데이터 추가
			PaymentMetadata.createFailedPaymentMetadata(payment, metadataJson);

			// 결제 정보 저장
			Payment savedPayment = paymentRepository.save(payment);

			// Redis에서 주문 정보 삭제
			redisRepository.remove(KEY_PREFIX + orderId);

			log.info("결제 실패 정보 저장 완료: orderId={}, errorCode={}", orderId, errorCode);
			return savedPayment;
		} catch (Exception e) {
			log.error("결제 실패 정보 저장 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 실패 정보 저장 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 주문 ID에 해당하는 결제 정보를 조회합니다.
	 *
	 * @param orderId 주문 ID
	 * @return 결제 정보
	 */
	private PaymentOrderInfo getPaymentOrderInfoByOrderId(String orderId) {
		String redisKey = KEY_PREFIX + orderId;
		Object value = redisRepository.get(redisKey);

		if (value == null) {
			throw new ServiceException(ErrorCode.ORDER_NOT_FOUND,
				"주문 ID에 해당하는 결제 정보를 찾을 수 없습니다: " + orderId);
		}

		return PaymentOrderInfo.fromMap((Map<String, Object>)value);
	}

	/**
	 * 결제 금액이 유효한지 검증합니다.
	 * 금액 조작이 감지되면 관련 정보를 저장합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param requestAmount 결제 요청 금액
	 */
	public void validatePaymentAmount(PaymentType paymentType, Long referenceId, BigDecimal requestAmount) {
		try {
			// 전략 패턴을 사용하여 결제 유형에 맞는 검증 로직 실행
			validatorFactory.getValidator(paymentType).validateAndGetExpectedAmount(referenceId, requestAmount);
		} catch (PaymentAmountManipulationException e) {
			// 금액 조작이 감지된 경우 관련 정보 저장
			savePaymentManipulationInfo(paymentType, e.getReferenceId(), e.getExpectedAmount(), e.getRequestAmount());

			// 예외를 다시 던져서 호출자에게 알림
			throw e;
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
		String responseBody) {
		try {
			// 토스페이먼츠 응답에서 paymentKey 추출
			Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
			String paymentKey = (String)responseMap.get("paymentKey");

			// 수수료 계산
			BigDecimal fee = calculateFee(amount);

			// 전략 패턴을 사용하여 결제 유형에 맞는 저장 로직 실행
			Payment payment = saverFactory.getSaver(paymentType)
				.savePayment(referenceId, amount, fee, paymentKey);

			// PaymentMetadata 엔티티 생성
			PaymentMetadata.createPaymentMetadata(payment, responseBody);

			// 결제 정보 저장
			paymentRepository.save(payment);

			log.info("결제 정보 저장 완료: paymentId={} paymentKey={}, amount={}, fee={}",
				payment.getId(), paymentKey, amount, fee);
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
		return infoProviderFactory.getProvider(payment.getPaymentType())
			.enrichPaymentResult(payment, baseResponse);
	}

	/**
	 * 결제 정보를 기반으로 결제 결과 응답을 생성합니다.
	 * 결제 유형에 따라 다른 추가 정보를 포함합니다.
	 *
	 * @param payment 결제 정보
	 * @param code 에러 코드
	 * @param message 에러 메시지
	 * @return 결제 결과 응답
	 */
	public PaymentResultResponse createFailedPaymentResultResponse(Payment payment, String code, String message) {
		// 기본 결제 결과 응답 생성
		PaymentResultResponse baseResponse = PaymentResultResponse.fail(payment.getTotalPrice(), code, message);

		// 결제 유형에 맞는 정보 제공자를 통해 응답 보강
		return infoProviderFactory.getProvider(payment.getPaymentType())
			.enrichPaymentResult(payment, baseResponse);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void savePaymentManipulationInfo(
		PaymentType paymentType,
		Long referenceId,
		BigDecimal expectedAmount,
		BigDecimal requestAmount) {

		log.warn("결제 금액 조작 감지: 유형={}, 참조ID={}, 예상금액={}, 요청금액={}",
			paymentType, referenceId, expectedAmount, requestAmount);

		// 클라이언트 정보 수집
		String ipAddress = "unknown";
		String userAgent = "unknown";

		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				ipAddress = getClientIp(request);
				userAgent = request.getHeader("User-Agent");
			}
		} catch (Exception ex) {
			log.error("클라이언트 정보 수집 중 오류 발생", ex);
		}

		try {
			Map<String, Object> additionalInfo = infoProviderFactory.getProvider(paymentType)
				.provideAdditionalInfo(referenceId);

			Contract contract = (Contract)additionalInfo.get("contract");
			String title = additionalInfo.get("title").toString();
			Member member = contract.getClient();

			// 결제 정보 생성
			Payment payment = Payment.createForAmountManipulation(paymentType, referenceId, member, title);

			// 조작 세부 정보 생성
			PaymentManipulationDetail manipulationDetail = PaymentManipulationDetail.create(
				expectedAmount, requestAmount, ipAddress, userAgent);

			// Payment에 조작 세부 정보 추가
			payment.addManipulationDetail(manipulationDetail);
			payment.markAsManipulated();

			// 저장 및 즉시 플러시
			payment = paymentRepository.saveAndFlush(payment);

			log.info("결제 금액 조작 정보 저장 완료: 결제ID={}", payment.getId());
		} catch (Exception ex) {
			log.error("결제 금액 조작 정보 저장 중 오류 발생", ex);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 금액 조작 정보 저장 중 오류 발생");
		}
	}

	/**
	 * 클라이언트 IP 주소를 가져옵니다.
	 */
	private String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}

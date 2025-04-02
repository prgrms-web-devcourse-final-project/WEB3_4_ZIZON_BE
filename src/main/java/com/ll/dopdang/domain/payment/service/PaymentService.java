package com.ll.dopdang.domain.payment.service;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentDetail;
import com.ll.dopdang.domain.payment.entity.PaymentMetadata;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.strategy.PaymentValidatorFactory;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.service.ContractService;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private static final String KEY_PREFIX = "payment:order:";
	private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";
	private static final long ORDER_EXPIRY_MINUTES = 10L;

	private final RedisRepository redisRepository;
	private final ContractService contractService;
	private final PaymentRepository paymentRepository;
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate;
	private final PaymentValidatorFactory validatorFactory;

	@Value("${payment.toss.secretKey}")
	private String tossSecretKey;

	/**
	 * 결제 유형과 참조 ID에 대한 주문 ID를 생성합니다.
	 * 이미 결제가 완료된 경우 ServiceException을 발생시킵니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @return 생성된 주문 ID
	 * @throws ServiceException 이미 결제가 완료된 경우
	 */
	public String createOrderId(PaymentType paymentType, Long referenceId) {
		// 이미 결제가 완료된 건인지 확인
		boolean alreadyPaid = paymentRepository.existsByPaymentTypeAndReferenceId(paymentType, referenceId);

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
		return orderId;
	}

	/**
	 * 토스페이먼츠 결제 승인을 처리합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 */
	@Transactional
	public void confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
		log.info("결제 승인 요청: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);

		// 주문 ID로 결제 정보 조회
		// Todo: 이미 결제된 건인지 검증하는 로직 추가
		PaymentOrderInfo orderInfo = getPaymentOrderInfoByOrderId(orderId);
		PaymentType paymentType = orderInfo.getPaymentType();
		Long referenceId = orderInfo.getReferenceId();

		// 결제 금액 검증
		validatePaymentAmount(paymentType, referenceId, amount);

		// 토스페이먼츠 API 호출
		String responseBody = callTossPaymentsApi(paymentKey, orderId, amount);

		// 결제 정보 저장
		savePaymentInformation(paymentType, referenceId, amount, responseBody);

		// Redis에서 주문 정보 삭제
		redisRepository.remove(KEY_PREFIX + orderId);

		log.info("결제 승인 성공: paymentKey={}", paymentKey);
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

		return (PaymentOrderInfo)value;
	}

	/**
	 * 결제 금액이 유효한지 검증합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param requestAmount 결제 요청 금액
	 */
	private void validatePaymentAmount(PaymentType paymentType, Long referenceId, BigDecimal requestAmount) {
		// 전략 패턴을 사용하여 결제 유형에 맞는 검증 로직 실행
		validatorFactory.getValidator(paymentType).validateAndGetExpectedAmount(referenceId, requestAmount);
	}

	/**
	 * 토스페이먼츠 API를 호출하여 결제를 승인합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param orderId 주문 ID
	 * @param amount 결제 금액
	 * @return API 응답 데이터
	 */
	private String callTossPaymentsApi(String paymentKey, String orderId, BigDecimal amount) {
		try {
			// HTTP 요청 헤더 설정
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Basic 인증 헤더 생성 (시크릿 키 + ":")
			String authString = tossSecretKey + ":";
			String encodedAuthString = Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
			headers.set("Authorization", "Basic " + encodedAuthString);

			// 요청 본문 생성
			Map<String, Object> requestBody = Map.of(
				"paymentKey", paymentKey,
				"orderId", orderId,
				"amount", amount
			);

			// HTTP 요청 엔티티 생성
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

			// API 호출
			ResponseEntity<String> response = restTemplate.postForEntity(
				URI.create(TOSS_API_URL),
				requestEntity,
				String.class
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
	 * 결제 정보를 저장합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param amount 결제 금액
	 * @param responseBody 토스페이먼츠 응답 데이터
	 */
	private void savePaymentInformation(PaymentType paymentType, Long referenceId, BigDecimal amount,
		String responseBody) {
		try {
			// 토스페이먼츠 응답에서 paymentKey 추출
			Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
			String paymentKey = (String)responseMap.get("paymentKey");

			// 수수료 계산
			BigDecimal fee = calculateFee(amount);

			Payment payment;
			PaymentDetail paymentDetail;

			switch (paymentType) {
				case PROJECT:
					Contract contract = contractService.getContractById(referenceId);

					// 1. Payment 엔티티 생성 (paymentKey 포함)
					payment = Payment.createFromContract(contract, amount, fee, paymentKey);

					// 2. PaymentDetail 엔티티 생성
					paymentDetail = PaymentDetail.createFromContract(payment, contract, amount, fee);
					payment.addPaymentDetail(paymentDetail);
					break;

				case ORDER:
					// Todo: 스토어 주문에 대한 저장 로직 추가 필요
					throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "주문 결제 처리 기능이 아직 구현되지 않았습니다.");

				case ETC:
					// Todo: 기타 결제 유형에 대한 저장 로직 추가 필요
					throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "기타 결제 처리 기능이 아직 구현되지 않았습니다.");

				default:
					throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR, "지원하지 않는 결제 유형입니다.");
			}

			// 3. PaymentMetadata 엔티티 생성
			PaymentMetadata metadata = PaymentMetadata.createMetadata(payment, responseBody);
			payment.setMetadata(metadata);

			// 결제 정보 저장 (cascade 설정에 따라 관련 엔티티도 함께 저장)
			paymentRepository.save(payment);

			log.info("결제 정보 저장 완료: paymentId={} paymentKey={}, amount={}, fee={}",
				payment.getId(), paymentKey, amount, fee);
		} catch (Exception e) {
			log.error("결제 정보 저장 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 정보 저장 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 결제 금액에 대한 수수료를 계산합니다.
	 * 현재는 결제 금액의 10%로 설정되어 있습니다.
	 *
	 * @param amount 결제 금액
	 * @return 계산된 수수료
	 */
	private BigDecimal calculateFee(BigDecimal amount) {
		return amount.multiply(new BigDecimal("0.1")); // 10% 수수료
	}
}

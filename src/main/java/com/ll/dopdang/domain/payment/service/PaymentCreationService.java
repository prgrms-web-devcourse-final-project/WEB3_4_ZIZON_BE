package com.ll.dopdang.domain.payment.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.strategy.info.PaymentInfoProviderFactory;
import com.ll.dopdang.domain.payment.util.PaymentConstants;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 생성 및 주문 ID 생성 관련 로직을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCreationService {

	// 상수를 PaymentConstants 클래스에서 참조
	private final RedisRepository redisRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentInfoProviderFactory infoProviderFactory;
	private final MemberService memberService;

	/**
	 * 결제 유형과 참조 ID에 대한 주문 ID를 생성하고 관련 정보를 반환합니다.
	 * 이미 결제가 완료된 경우 ServiceException을 발생시킵니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param memberId 회원 ID
	 * @param quantity 수량 (선택적)
	 * @return 주문 ID와 고객 키, 결제 유형별 추가 정보가 포함된 맵
	 * @throws ServiceException 이미 결제가 완료된 경우
	 */
	public Map<String, Object> createOrderIdWithInfo(PaymentType paymentType, Long referenceId, Long memberId,
		Integer quantity) {

		String orderId = generateOrderId();
		String redisKey = PaymentConstants.KEY_PREFIX + orderId;

		PaymentOrderInfo orderInfo = new PaymentOrderInfo(paymentType, referenceId, orderId, quantity);
		redisRepository.save(redisKey, orderInfo, PaymentConstants.ORDER_EXPIRY_MINUTES, TimeUnit.MINUTES);

		log.info("주문 ID 생성 완료: orderId={}, paymentType={}, referenceId={}, quantity={}",
			orderId, paymentType, referenceId, quantity);

		Member member = memberService.getMemberById(memberId);
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
	 * 고유한 주문 ID를 생성합니다.
	 *
	 * @return 생성된 주문 ID
	 */
	private String generateOrderId() {
		return UUID.randomUUID().toString();
	}
}

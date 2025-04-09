package com.ll.dopdang.domain.payment.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.util.PaymentConstants;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.redis.repository.RedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 조회 관련 로직을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

	private final RedisRepository redisRepository;

	/**
	 * 주문 ID에 해당하는 결제 정보를 조회합니다.
	 *
	 * @param orderId 주문 ID
	 * @return 결제 정보
	 */
	public PaymentOrderInfo getPaymentOrderInfoByOrderId(String orderId) {
		String redisKey = PaymentConstants.KEY_PREFIX + orderId;
		Object value = redisRepository.get(redisKey);

		if (value == null) {
			throw new ServiceException(ErrorCode.ORDER_NOT_FOUND,
				"주문 ID에 해당하는 결제 정보를 찾을 수 없습니다: " + orderId);
		}

		return PaymentOrderInfo.fromMap((Map<String, Object>)value);
	}
}

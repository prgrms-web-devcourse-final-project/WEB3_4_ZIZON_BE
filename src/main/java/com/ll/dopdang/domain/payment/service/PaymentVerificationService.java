package com.ll.dopdang.domain.payment.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentManipulationDetail;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.strategy.info.PaymentInfoProviderFactory;
import com.ll.dopdang.domain.payment.strategy.validator.PaymentValidatorFactory;
import com.ll.dopdang.domain.payment.util.PaymentUtils;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.PaymentAmountManipulationException;
import com.ll.dopdang.global.exception.ServiceException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 검증 관련 로직을 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVerificationService {

	private final PaymentValidatorFactory validatorFactory;
	private final PaymentInfoProviderFactory infoProviderFactory;
	private final MemberService memberService;
	private final PaymentRepository paymentRepository;

	/**
	 * 결제 금액이 유효한지 검증합니다.
	 * 금액 조작이 감지되면 관련 정보를 저장합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param requestAmount 결제 요청 금액
	 */
	public void validatePaymentAmount(PaymentType paymentType, Long referenceId, BigDecimal requestAmount,
		String orderId) {
		try {
			// 전략 패턴을 사용하여 결제 유형에 맞는 검증 로직 실행
			validatorFactory.getValidator(paymentType)
				.validateAndGetExpectedAmount(referenceId, requestAmount, orderId);
		} catch (PaymentAmountManipulationException e) {
			// 금액 조작이 감지된 경우 관련 정보 저장
			savePaymentManipulationInfo(paymentType, e.getReferenceId(), e.getExpectedAmount(), e.getRequestAmount(),
				e.getOrderId());

			// 예외를 다시 던져서 호출자에게 알림
			throw e;
		}
	}

	/**
	 * 결제 금액 조작 정보를 저장합니다.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void savePaymentManipulationInfo(
		PaymentType paymentType,
		Long referenceId,
		BigDecimal expectedAmount,
		BigDecimal requestAmount,
		String orderId) {

		log.warn("결제 금액 조작 감지: 유형={}, 참조ID={}, 예상금액={}, 요청금액={}",
			paymentType, referenceId, expectedAmount, requestAmount);

		// 클라이언트 정보 수집
		String ipAddress = "unknown";
		String userAgent = "unknown";

		try {
			ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
				HttpServletRequest request = attributes.getRequest();
				ipAddress = PaymentUtils.getClientIp(request);
				userAgent = request.getHeader("User-Agent");
			}
		} catch (Exception ex) {
			log.error("클라이언트 정보 수집 중 오류 발생", ex);
		}

		try {
			Map<String, Object> additionalInfo;

			// 모든 결제 유형에 대해 orderId를 포함하여 추가 정보 조회
			additionalInfo = infoProviderFactory.getProvider(paymentType)
				.provideAdditionalInfo(referenceId, orderId);

			String title = additionalInfo.get("title").toString();
			Member member = memberService.getMemberById((Long)additionalInfo.get("clientId"));

			// 결제 정보 생성
			Payment payment = Payment.createForAmountManipulation(paymentType, referenceId, member, title, orderId);

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
}

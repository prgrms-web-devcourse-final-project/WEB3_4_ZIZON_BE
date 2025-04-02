package com.ll.dopdang.domain.payment.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.payment.client.TossPaymentClient;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentCancellationDetail;
import com.ll.dopdang.domain.payment.entity.PaymentMetadata;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.util.TossPaymentUtils;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 취소 관련 기능을 제공하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancellationService {

	private final PaymentRepository paymentRepository;
	private final TossPaymentClient tossPaymentClient;

	@Value("${payment.toss.secretKey}")
	private String tossSecretKey;

	/**
	 * 결제 유형과 참조 ID로 결제를 취소합니다.
	 * 취소 금액이 null이거나 결제 총액과 같으면 전액 취소로 처리합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param cancelReason 취소 사유
	 * @param cancelAmount 취소 금액 (null인 경우 전액 취소)
	 * @return 취소된 결제 정보
	 */
	@Transactional
	public Payment cancelPayment(
		PaymentType paymentType, Long referenceId, String cancelReason, BigDecimal cancelAmount) {

		log.info("결제 취소 요청(참조 정보): paymentType={}, referenceId={}, cancelReason={}, cancelAmount={}",
			paymentType, referenceId, cancelReason, cancelAmount);

		// 결제 정보 조회
		Payment payment = paymentRepository.findByPaymentTypeAndReferenceId(paymentType, referenceId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PAYMENT_NOT_FOUND,
				"결제 정보를 찾을 수 없습니다: " + paymentType + ", " + referenceId));

		// 이미 취소된 결제인지 확인
		if (payment.isCanceled()) {
			throw new ServiceException(ErrorCode.PAYMENT_ALREADY_CANCELED,
				"이미 취소된 결제입니다: " + payment.getId());
		}

		// 전액 취소 여부 확인
		boolean isFullCancellation = (cancelAmount == null);
		BigDecimal amountToCancel;

		if (isFullCancellation) {
			// 전액 취소
			amountToCancel = payment.getRemainingAmount();
		} else {
			// 부분 취소 - 금액 유효성 검증
			if (cancelAmount.compareTo(BigDecimal.ZERO) <= 0
				|| cancelAmount.compareTo(payment.getRemainingAmount()) > 0) {
				throw new ServiceException(ErrorCode.INVALID_CANCEL_AMOUNT,
					"유효하지 않은 취소 금액입니다: " + cancelAmount);
			}
			amountToCancel = cancelAmount;

			// 남은 금액과 동일하면 전액 취소로 처리
			if (amountToCancel.compareTo(payment.getRemainingAmount()) == 0) {
				isFullCancellation = true;
			}
		}

		// 토스페이먼츠 API 호출하여 결제 취소
		String responseBody = callTossPaymentsCancelApi(
			payment.getPaymentKey(),
			cancelReason,
			isFullCancellation ? null : amountToCancel
		);

		// 취소 정보 저장
		processCancellation(payment, responseBody, amountToCancel, cancelReason, isFullCancellation);

		log.info("결제 취소 성공: paymentId={}, paymentKey={}, cancelAmount={}, isFullCancellation={}",
			payment.getId(), payment.getPaymentKey(), amountToCancel, isFullCancellation);

		return payment;
	}

	/**
	 * 토스페이먼츠 결제 취소 API를 호출합니다.
	 *
	 * @param paymentKey 결제 키
	 * @param cancelReason 취소 사유
	 * @param cancelAmount 취소 금액 (부분 취소 시에만 사용, 전액 취소 시 null)
	 * @return API 응답 데이터
	 */
	private String callTossPaymentsCancelApi(String paymentKey, String cancelReason, BigDecimal cancelAmount) {
		return TossPaymentUtils.cancelPayment(
			tossPaymentClient,
			tossSecretKey,
			paymentKey,
			cancelReason,
			cancelAmount
		);
	}

	/**
	 * 결제 취소 정보를 처리합니다.
	 *
	 * @param payment 결제 정보
	 * @param responseBody 토스페이먼츠 응답 데이터
	 * @param cancelAmount 취소 금액
	 * @param cancelReason 취소 사유
	 * @param isFullCancellation 전액 취소 여부
	 */
	private void processCancellation(Payment payment, String responseBody,
		BigDecimal cancelAmount, String cancelReason, boolean isFullCancellation) {
		try {
			// 1. Payment 상태 업데이트
			payment.updateCancellationStatus(cancelAmount, isFullCancellation);

			// 2. PaymentCancellationDetail 생성 및 추가
			PaymentCancellationDetail.createCancellationDetail(
				payment, cancelAmount, cancelReason, isFullCancellation);

			// 3. 취소 메타데이터 생성 - 토스 응답 그대로 저장
			PaymentMetadata.createCancellationMetadata(payment, responseBody);

			// 결제 정보 저장
			paymentRepository.save(payment);

			log.info("결제 취소 정보 저장 완료: paymentId={}, cancelAmount={}, isFullCancellation={}",
				payment.getId(), cancelAmount, isFullCancellation);
		} catch (Exception e) {
			log.error("결제 취소 정보 저장 중 오류 발생", e);
			throw new ServiceException(ErrorCode.PAYMENT_PROCESSING_ERROR,
				"결제 취소 정보 저장 중 오류가 발생했습니다.");
		}
	}
}

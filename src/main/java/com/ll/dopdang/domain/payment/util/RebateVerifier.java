package com.ll.dopdang.domain.payment.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.Rebate;
import com.ll.dopdang.domain.payment.entity.RebateStatus;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.repository.RebateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 결과를 검증하는 유틸리티 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RebateVerifier {

	private final RebateRepository rebateRepository;
	private final PaymentRepository paymentRepository;

	@Value("${payment.fee.rate}")
	private BigDecimal feeRate;

	/**
	 * 특정 월의 정산 데이터 검증
	 *
	 * @param yearMonth 검증할 년월
	 * @return 검증 결과
	 */
	public Map<String, Object> verifyMonthlyRebate(YearMonth yearMonth) {
		String yearMonthStr = yearMonth.toString();
		log.info("정산 데이터 검증 시작: {}", yearMonthStr);

		// 해당 월의 정산 데이터 조회
		List<Rebate> rebates = rebateRepository.findByRebateYearMonth(yearMonthStr);

		// 해당 월의 결제 데이터 조회
		LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
		LocalDateTime endDateTime = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
		List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDateTime, endDateTime);

		// 결제 건수와 정산 건수 비교
		long eligiblePaymentCount = countEligiblePayments(payments);

		// 기본 결과 맵 생성
		Map<String, Object> result = createBaseResultMap(yearMonthStr, rebates);
		result.put("totalPayments", payments.size());
		result.put("eligiblePayments", eligiblePaymentCount);

		// 불일치 항목 확인
		List<String> discrepancies = new ArrayList<>();

		// 결제 건수와 정산 건수 비교
		if (eligiblePaymentCount != rebates.size()) {
			discrepancies.add(String.format(
				"결제 건수와 정산 건수가 일치하지 않습니다. (적격 결제: %d건, 정산: %d건)",
				eligiblePaymentCount, rebates.size()));
		}

		// 각 정산 데이터의 금액 검증
		List<Map<String, Object>> incorrectRebates = verifyRebateAmounts(rebates);
		addDiscrepancyIfNeeded(discrepancies, incorrectRebates, "일부 정산 데이터의 금액이 올바르지 않습니다. (%d건)");
		if (!incorrectRebates.isEmpty()) {
			result.put("incorrectRebates", incorrectRebates);
		}

		// 정산되지 않은 결제 확인
		List<Map<String, Object>> unprocessedPayments = findUnprocessedPayments(payments, rebates);
		addDiscrepancyIfNeeded(discrepancies, unprocessedPayments, "일부 적격 결제가 정산되지 않았습니다. (%d건)");
		if (!unprocessedPayments.isEmpty()) {
			result.put("unprocessedPayments", unprocessedPayments);
		}

		// 최종 결과 설정
		finalizeResult(result, discrepancies);

		log.info("정산 데이터 검증 완료: {} (유효: {})", yearMonthStr, discrepancies.isEmpty());
		return result;
	}

	/**
	 * 특정 전문가의 정산 데이터 검증
	 *
	 * @param expertId 전문가 ID
	 * @param yearMonth 검증할 년월
	 * @return 검증 결과
	 */
	public Map<String, Object> verifyExpertRebate(Long expertId, YearMonth yearMonth) {
		String yearMonthStr = yearMonth.toString();
		log.info("전문가 정산 데이터 검증 시작: expertId={}, yearMonth={}", expertId, yearMonthStr);

		// 해당 전문가의 정산 데이터 조회
		List<Rebate> rebates = rebateRepository.findByExpertIdAndRebateYearMonth(expertId, yearMonthStr);

		// 기본 결과 맵 생성
		Map<String, Object> result = createBaseResultMap(yearMonthStr, rebates);
		result.put("expertId", expertId);

		// 데이터가 없는 경우 처리
		if (rebates.isEmpty()) {
			List<String> discrepancies = List.of("해당 전문가의 정산 데이터가 없습니다.");
			finalizeResult(result, discrepancies);
			return result;
		}

		// 각 정산 데이터의 금액 검증
		List<Map<String, Object>> incorrectRebates = verifyRebateAmounts(rebates);

		// 불일치 항목 확인
		List<String> discrepancies = new ArrayList<>();
		addDiscrepancyIfNeeded(discrepancies, incorrectRebates, "일부 정산 데이터의 금액이 올바르지 않습니다. (%d건)");

		if (!incorrectRebates.isEmpty()) {
			result.put("incorrectRebates", incorrectRebates);
		}

		// 최종 결과 설정
		finalizeResult(result, discrepancies);

		log.info("전문가 정산 데이터 검증 완료: expertId={}, yearMonth={} (유효: {})",
			expertId, yearMonthStr, discrepancies.isEmpty());
		return result;
	}

	/**
	 * 적격 결제 건수를 계산
	 */
	private long countEligiblePayments(List<Payment> payments) {
		return payments.stream()
			.filter(p -> p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.PARTIALLY_CANCELED)
			.count();
	}

	/**
	 * 기본 결과 맵 생성
	 */
	private Map<String, Object> createBaseResultMap(String yearMonthStr, List<Rebate> rebates) {
		// 정산 금액 합계 계산
		BigDecimal totalRebateAmount = rebates.stream()
			.map(Rebate::getRebateAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalFeeAmount = rebates.stream()
			.map(Rebate::getFeeAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		// 상태별 건수
		Map<RebateStatus, Long> statusCounts = rebates.stream()
			.collect(Collectors.groupingBy(Rebate::getStatus, Collectors.counting()));

		// 결과 맵 생성
		Map<String, Object> result = new HashMap<>();
		result.put("yearMonth", yearMonthStr);
		result.put("totalRebates", rebates.size());
		result.put("totalRebateAmount", totalRebateAmount);
		result.put("totalFeeAmount", totalFeeAmount);
		result.put("statusCounts", statusCounts);

		return result;
	}

	/**
	 * 정산 금액 검증
	 */
	private List<Map<String, Object>> verifyRebateAmounts(List<Rebate> rebates) {
		List<Map<String, Object>> incorrectRebates = new ArrayList<>();

		for (Rebate rebate : rebates) {
			Payment payment = rebate.getPayment();
			BigDecimal remainingAmount = payment.getRemainingAmount();
			BigDecimal expectedFeeAmount = calculateExpectedFeeAmount(remainingAmount);
			BigDecimal expectedRebateAmount = remainingAmount.subtract(expectedFeeAmount);

			if (!rebate.getFeeAmount().equals(expectedFeeAmount)
				|| !rebate.getRebateAmount().equals(expectedRebateAmount)) {

				Map<String, Object> incorrectRebate = new HashMap<>();
				incorrectRebate.put("rebateId", rebate.getId());
				incorrectRebate.put("paymentId", payment.getId());
				incorrectRebate.put("actualFeeAmount", rebate.getFeeAmount());
				incorrectRebate.put("expectedFeeAmount", expectedFeeAmount);
				incorrectRebate.put("actualRebateAmount", rebate.getRebateAmount());
				incorrectRebate.put("expectedRebateAmount", expectedRebateAmount);
				incorrectRebate.put("remainingAmount", remainingAmount);

				// 전문가 정보 추가 (있는 경우)
				if (rebate.getExpert() != null) {
					incorrectRebate.put("expertId", rebate.getExpert().getId());
					incorrectRebate.put("expertName", rebate.getExpert().getName());
				}

				incorrectRebates.add(incorrectRebate);
			}
		}

		return incorrectRebates;
	}

	/**
	 * 예상 수수료 금액 계산
	 */
	private BigDecimal calculateExpectedFeeAmount(BigDecimal amount) {
		return amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * 정산되지 않은 결제 찾기
	 */
	private List<Map<String, Object>> findUnprocessedPayments(List<Payment> payments, List<Rebate> rebates) {
		List<Map<String, Object>> unprocessedPayments = new ArrayList<>();

		for (Payment payment : payments) {
			if (isEligibleForRebate(payment)
				&& rebates.stream().noneMatch(r -> r.getPayment().getId().equals(payment.getId()))) {

				Map<String, Object> unprocessedPayment = new HashMap<>();
				unprocessedPayment.put("paymentId", payment.getId());
				unprocessedPayment.put("paymentKey", payment.getPaymentKey());
				unprocessedPayment.put("originalAmount", payment.getTotalPrice());
				unprocessedPayment.put("remainingAmount", payment.getRemainingAmount());
				unprocessedPayment.put("paymentDate", payment.getPaymentDate());
				unprocessedPayment.put("status", payment.getStatus());
				unprocessedPayment.put("paymentType", payment.getPaymentType());
				unprocessedPayment.put("referenceId", payment.getReferenceId());

				unprocessedPayments.add(unprocessedPayment);
			}
		}

		return unprocessedPayments;
	}

	/**
	 * 결제가 정산 대상인지 확인
	 */
	private boolean isEligibleForRebate(Payment payment) {
		return payment.getStatus() == PaymentStatus.PAID
			|| payment.getStatus() == PaymentStatus.PARTIALLY_CANCELED;
	}

	/**
	 * 불일치 항목이 있으면 추가
	 */
	private void addDiscrepancyIfNeeded(List<String> discrepancies, List<?> items, String format) {
		if (!items.isEmpty()) {
			discrepancies.add(String.format(format, items.size()));
		}
	}

	/**
	 * 최종 결과 설정
	 */
	private void finalizeResult(Map<String, Object> result, List<String> discrepancies) {
		result.put("discrepancies", discrepancies);
		result.put("isValid", discrepancies.isEmpty());
	}
}

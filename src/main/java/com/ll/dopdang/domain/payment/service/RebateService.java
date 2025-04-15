package com.ll.dopdang.domain.payment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.service.ExpertService;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.Rebate;
import com.ll.dopdang.domain.payment.entity.RebateStatus;
import com.ll.dopdang.domain.payment.repository.PaymentRepository;
import com.ll.dopdang.domain.payment.repository.RebateRepository;
import com.ll.dopdang.domain.payment.strategy.info.PaymentInfoProviderFactory;
import com.ll.dopdang.domain.payment.strategy.info.PaymentOrderInfoProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 처리를 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RebateService {

	private final PaymentRepository paymentRepository;
	private final RebateRepository rebateRepository;
	private final ExpertService expertService;
	private final PaymentInfoProviderFactory paymentInfoProviderFactory;

	@Value("${payment.fee.rate}")
	private BigDecimal feeRate;

	/**
	 * 매일 자정에 전일 결제 건에 대한 정산 데이터 생성
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void createDailyRebateData() {
		// 전일 계산
		YearMonth currentMonth = YearMonth.now();
		LocalDateTime startDateTime = LocalDate.now().minusDays(1).atStartOfDay();
		LocalDateTime endDateTime = LocalDate.now().atStartOfDay();

		log.info("정산 데이터 생성 시작: {} - {}", startDateTime, endDateTime);

		int createdCount = createRebateDataForPeriod(startDateTime, endDateTime, currentMonth);

		log.info("정산 데이터 생성 완료: {} 건", createdCount);
	}

	/**
	 * 특정 기간에 대한 정산 데이터 생성
	 *
	 * @param startDateTime 시작 날짜 및 시간
	 * @param endDateTime 종료 날짜 및 시간
	 * @param yearMonth 정산 대상 년월
	 * @return 생성된 정산 데이터 수
	 */
	@Transactional
	public int createRebateDataForPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime, YearMonth yearMonth) {
		// 해당 기간에 결제 완료된 건들 조회
		List<Payment> eligiblePayments = getEligiblePayments(startDateTime, endDateTime);

		// 이미 정산 데이터가 있는 결제는 제외
		eligiblePayments = filterExistingRebates(eligiblePayments);

		// 정산 데이터 생성
		List<Rebate> rebates = createRebatesFromPayments(eligiblePayments, yearMonth);

		// 저장
		rebateRepository.saveAll(rebates);

		return rebates.size();
	}

	/**
	 * 매월 15일 오전 2시에 정산 처리 실행
	 * 스케줄러가 실행하는 메서드.
	 */
	@Scheduled(cron = "0 0 2 15 * ?")
	@Transactional
	public void processMonthlyRebate() {
		// 전월 계산
		YearMonth previousMonth = YearMonth.now().minusMonths(1);

		processMonthlyRebate(previousMonth);
	}

	/**
	 * 수동으로 정산 처리 실행
	 *
	 * @param yearMonth 정산 대상 년월
	 */
	@Transactional
	public void processMonthlyRebate(YearMonth yearMonth) {
		String yearMonthStr = yearMonth.toString();

		log.info("정산 처리 시작: {}", yearMonthStr);

		// 정산 대기 상태인 건들 조회
		List<Rebate> pendingRebates = rebateRepository.findByRebateYearMonthAndStatus(
			yearMonthStr, RebateStatus.PENDING);

		log.info("정산 처리 대상 건수: {}", pendingRebates.size());

		// 정산 처리
		processRebates(pendingRebates);

		log.info("정산 처리 완료: {}", yearMonthStr);
	}

	/**
	 * 정산 처리 실행
	 *
	 * @param rebates 처리할 정산 목록
	 */
	private void processRebates(List<Rebate> rebates) {
		for (Rebate rebate : rebates) {
			try {
				// 여기에 실제 정산 처리 로직 구현 (외부 API 호출 등)
				// 정산 완료 처리
				rebate.complete();
			} catch (Exception e) {
				log.error("정산 처리 실패: {}", rebate.getId(), e);
				rebate.fail();
			}
		}

		rebateRepository.saveAll(rebates);
	}

	/**
	 * 수동으로 정산 데이터 생성
	 *
	 * @param yearMonth 정산 대상 년월
	 * @return 생성된 정산 데이터 수
	 */
	public int createRebateDataManually(YearMonth yearMonth) {
		return createRebateData(yearMonth);
	}

	/**
	 * 정산 데이터 생성 공통 로직
	 *
	 * @param yearMonth 정산 대상 년월
	 * @return 생성된 정산 데이터 수
	 */
	public int createRebateData(YearMonth yearMonth) {
		YearMonth nextMonth = yearMonth.plusMonths(1);

		LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
		LocalDateTime endDateTime = nextMonth.atDay(1).atStartOfDay();

		// 해당 월에 결제 완료된 건들 조회
		List<Payment> eligiblePayments = getEligiblePayments(startDateTime, endDateTime);

		// 이미 정산 데이터가 있는 결제는 제외
		eligiblePayments = filterExistingRebates(eligiblePayments);

		// 정산 데이터 생성
		List<Rebate> rebates = createRebatesFromPayments(eligiblePayments, yearMonth);

		// 저장
		rebateRepository.saveAll(rebates);

		return rebates.size();
	}

	/**
	 * 정산 대상 결제 목록 조회
	 */
	private List<Payment> getEligiblePayments(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		// 결제 완료된 건들 조회
		List<Payment> eligiblePayments = paymentRepository.findByPaymentDateBetweenAndStatus(
			startDateTime, endDateTime, PaymentStatus.PAID);

		// 부분 취소된 건도 포함
		eligiblePayments.addAll(paymentRepository.findByPaymentDateBetweenAndStatus(
			startDateTime, endDateTime, PaymentStatus.PARTIALLY_CANCELED));

		return eligiblePayments;
	}

	/**
	 * 이미 정산 데이터가 있는 결제 필터링
	 */
	private List<Payment> filterExistingRebates(List<Payment> payments) {
		return payments.stream()
			.filter(payment -> rebateRepository.findByPayment(payment).isEmpty())
			.collect(Collectors.toList());
	}

	/**
	 * 결제 목록으로부터 정산 데이터 생성
	 */
	private List<Rebate> createRebatesFromPayments(List<Payment> payments, YearMonth yearMonth) {
		return payments.stream()
			.map(payment -> {
				PaymentOrderInfoProvider infoProvider = paymentInfoProviderFactory
					.getProvider(payment.getPaymentType());                  // 결제 유형에 맞는 정보 제공자 가져오기
				Map<String, Object> additionalInfo = infoProvider
					.provideAdditionalInfo(payment.getReferenceId(),
						payment.getOrderId());        // 추가 정보 조회 (전문가 정보 포함)
				Expert expert = expertService.findExpertById((Long)additionalInfo.get("expertId"));
				return Rebate.createFromPayment(payment, feeRate, expert, yearMonth); // Rebate 생성
			}).collect(Collectors.toList());
	}
}

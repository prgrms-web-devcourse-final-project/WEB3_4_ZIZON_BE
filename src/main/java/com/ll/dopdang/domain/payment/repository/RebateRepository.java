package com.ll.dopdang.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.Rebate;
import com.ll.dopdang.domain.payment.entity.RebateStatus;

@Repository
public interface RebateRepository extends JpaRepository<Rebate, Long> {

	/**
	 * 특정 결제에 대한 정산 정보 조회
	 */
	Optional<Rebate> findByPayment(Payment payment);

	/**
	 * 특정 년월의 정산 목록 조회
	 */
	List<Rebate> findByRebateYearMonth(String rebateYearMonth);

	/**
	 * 특정 년월 및 상태의 정산 목록 조회
	 */
	List<Rebate> findByRebateYearMonthAndStatus(String rebateYearMonth, RebateStatus status);

	/**
	 * 특정 전문가 ID 및 년월의 정산 목록 조회
	 */
	List<Rebate> findByExpertIdAndRebateYearMonth(Long expertId, String rebateYearMonth);
}

package com.ll.dopdang.domain.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.PaymentType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	/**
	 * 결제 유형과 참조 ID로 결제 정보 조회
	 */
	Optional<Payment> findByPaymentTypeAndReferenceId(PaymentType paymentType, Long referenceId);

	/**
	 * 특정 회원의 결제 목록 조회
	 */
	List<Payment> findByMember(Member member);

	/**
	 * 특정 기간 내의 결제 목록 조회
	 */
	List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

	/**
	 * 특정 기간 및 상태의 결제 목록 조회
	 */
	List<Payment> findByPaymentDateBetweenAndStatus(
		LocalDateTime start,
		LocalDateTime end,
		PaymentStatus status
	);

	/**
	 * 결제 유형, 참조 ID, 결제 상태로 결제 정보를 조회합니다.
	 *
	 * @param paymentType 결제 유형
	 * @param referenceId 참조 ID
	 * @param status 결제 상태
	 * @return 조회된 결제 정보
	 */
	Optional<Payment> findByPaymentTypeAndReferenceIdAndStatus(
		PaymentType paymentType, Long referenceId, PaymentStatus status);

	/**
	 * 주문 ID로 결제 정보를 조회합니다.
	 */
	Optional<Payment> findByOrderId(String orderId);
}

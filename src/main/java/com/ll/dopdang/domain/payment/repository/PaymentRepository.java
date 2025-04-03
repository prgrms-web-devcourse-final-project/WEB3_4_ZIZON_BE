package com.ll.dopdang.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.PaymentType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByPaymentTypeAndReferenceId(PaymentType paymentType, Long referenceId);

	Optional<Payment> findByPaymentTypeAndReferenceId(PaymentType paymentType, Long referenceId);

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
}

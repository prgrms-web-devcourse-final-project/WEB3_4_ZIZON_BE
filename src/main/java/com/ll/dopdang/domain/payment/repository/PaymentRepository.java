package com.ll.dopdang.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	boolean existsByPaymentTypeAndReferenceId(PaymentType paymentType, Long referenceId);
}

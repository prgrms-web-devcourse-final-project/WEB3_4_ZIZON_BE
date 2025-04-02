package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentDetail;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.service.ContractService;

import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 결제 정보 저장 전략 구현
 */
@Component
@RequiredArgsConstructor
public class ProjectPaymentSaver implements PaymentSaver {

	private final ContractService contractService;

	@Override
	public Payment savePayment(Long referenceId, BigDecimal amount, BigDecimal fee, String paymentKey) {
		Contract contract = contractService.getContractById(referenceId);

		// 1. Payment 엔티티 생성 (paymentKey 포함)
		Payment payment = Payment.createFromContract(contract, amount, fee, paymentKey);

		// 2. PaymentDetail 엔티티 생성
		PaymentDetail paymentDetail = PaymentDetail.createFromContract(payment, contract, amount, fee);
		payment.addPaymentDetail(paymentDetail);

		return payment;
	}
}

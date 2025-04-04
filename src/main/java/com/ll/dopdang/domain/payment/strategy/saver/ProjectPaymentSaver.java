package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
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
	@Value("${payment.fee.rate}")
	private BigDecimal feeRate;

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

	@Override
	public Payment saveFailedPayment(Long referenceId, String errorCode, String errorMessage) {
		Contract contract = contractService.getContractById(referenceId);
		BigDecimal fee = contract.getPrice().multiply(feeRate); // 10% 수수료

		// 1. 결제 실패한 Payment 엔티티 생성
		Payment payment = Payment.createFailedPaymentFromContract(contract, fee);

		// 2. 결제 실패한 PaymentDetail 엔티티 생성
		PaymentDetail paymentDetail = PaymentDetail.createFailedPaymentDetail(payment, contract, fee);
		payment.addPaymentDetail(paymentDetail);

		return payment;
	}
}

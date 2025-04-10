package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentDetail;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.service.PaymentQueryService;
import com.ll.dopdang.domain.store.dto.ProductDetailResponse;
import com.ll.dopdang.domain.store.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 결제 정보 저장 전략 구현
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPaymentSaver implements PaymentSaver {

	private final ProductService productService;
	private final PaymentQueryService paymentQueryService;
	private final MemberService memberService;

	@Override
	public Payment savePayment(Long referenceId, BigDecimal amount, BigDecimal fee, String paymentKey, String orderId) {
		log.info("주문 결제 정보 저장: referenceId={}, amount={}, fee={}, paymentKey={}, orderId={}",
			referenceId, amount, fee, paymentKey, orderId);

		ProductDetailResponse productDetail = productService.getProductById(referenceId);
		String title = productDetail.getTitle();

		PaymentOrderInfo paymentOrderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
		Integer quantity = paymentOrderInfo.getQuantity();
		Member member = memberService.getMemberById(paymentOrderInfo.getMemberId());

		// 주문 결제 정보 생성
		Payment payment = Payment.builder()
			.member(member)
			.paymentType(PaymentType.ORDER)
			.paymentKey(paymentKey)
			.referenceId(referenceId)
			.itemsSummary(title)
			.itemsCount(quantity)
			.installmentMonths(0)
			.totalPrice(amount)
			.totalFee(fee)
			.paymentDate(LocalDateTime.now())
			.orderId(orderId)
			.status(PaymentStatus.PAID)
			.build();

		// 결제 상세 정보 생성
		PaymentDetail paymentDetail = PaymentDetail.builder()
			.itemType("ORDER")
			.itemId(referenceId)
			.itemName(title)
			.quantity(quantity)
			.unitPrice(productDetail.getPrice())
			.unitTotalPrice(amount)
			.feePrice(fee)
			.build();

		payment.addPaymentDetail(paymentDetail);

		return payment;
	}

	@Override
	public Payment saveFailedPayment(Long referenceId, String errorCode, String errorMessage, String orderId) {
		log.info("주문 결제 실패 정보 저장: referenceId={}, errorCode={}, errorMessage={}, orderId={}",
			referenceId, errorCode, errorMessage, orderId);

		ProductDetailResponse product = productService.getProductById(referenceId);
		String title = product.getTitle();

		PaymentOrderInfo paymentOrderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
		Integer quantity = paymentOrderInfo.getQuantity();
		Member member = memberService.getMemberById(paymentOrderInfo.getMemberId());

		// 실패한 주문 결제 정보 생성
		Payment payment = Payment.builder()
			.member(member)
			.orderId(orderId)
			.paymentType(PaymentType.ORDER)
			.referenceId(referenceId)
			.itemsSummary(title)
			.itemsCount(quantity)
			.installmentMonths(0)
			.totalPrice(BigDecimal.ZERO)
			.totalFee(BigDecimal.ZERO)
			.paymentDate(LocalDateTime.now())
			.status(PaymentStatus.FAILED)
			.build();

		// 실패한 결제 상세 정보 생성
		PaymentDetail paymentDetail = PaymentDetail.builder()
			.itemType("ORDER")
			.itemId(referenceId)
			.itemName(title)
			.quantity(quantity)
			.unitPrice(BigDecimal.ZERO)
			.unitTotalPrice(BigDecimal.ZERO)
			.feePrice(BigDecimal.ZERO)
			.build();

		payment.addPaymentDetail(paymentDetail);

		return payment;
	}
}

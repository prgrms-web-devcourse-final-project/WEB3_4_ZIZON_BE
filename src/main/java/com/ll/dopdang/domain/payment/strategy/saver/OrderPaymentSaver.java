package com.ll.dopdang.domain.payment.strategy.saver;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberService;
import com.ll.dopdang.domain.payment.dto.PaymentOrderInfo;
import com.ll.dopdang.domain.payment.entity.Payment;
import com.ll.dopdang.domain.payment.entity.PaymentDetail;
import com.ll.dopdang.domain.payment.entity.PaymentStatus;
import com.ll.dopdang.domain.payment.entity.PaymentType;
import com.ll.dopdang.domain.payment.service.PaymentQueryService;
import com.ll.dopdang.domain.store.dto.ProductDetailResponse;
import com.ll.dopdang.domain.store.entity.Order;
import com.ll.dopdang.domain.store.entity.OrderItem;
import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.repository.OrderItemRepository;
import com.ll.dopdang.domain.store.repository.OrderRepository;
import com.ll.dopdang.domain.store.service.ProductService;
import com.ll.dopdang.standard.util.LogSanitizer;

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
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	@Override
	@Transactional
	public Payment savePayment(Long referenceId, BigDecimal amount, BigDecimal fee, String paymentKey, String orderId) {
		log.info("주문 결제 정보 저장: referenceId={}, amount={}, fee={}, paymentKey={}, orderId={}",
			referenceId, amount, fee, LogSanitizer.sanitizeLogInput(paymentKey), orderId);

		ProductDetailResponse productDetail = productService.getProductById(referenceId);
		String title = productDetail.title();

		PaymentOrderInfo paymentOrderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
		Integer quantity = paymentOrderInfo.quantity();
		Member member = memberService.getMemberById(paymentOrderInfo.memberId());

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
			.unitPrice(productDetail.price())
			.unitTotalPrice(amount)
			.feePrice(fee)
			.build();

		payment.addPaymentDetail(paymentDetail);

		// 주문 정보 저장
		Product product = productService.findById(referenceId);
		Order order = Order.createOrder(member, orderId, amount);
		Order savedOrder = orderRepository.save(order);

		// 주문 아이템 정보 저장
		OrderItem orderItem = OrderItem.createOrderItem(savedOrder, product, quantity, productDetail.price());
		orderItemRepository.save(orderItem);

		log.info("주문 정보 저장 완료: orderId={}, productId={}, quantity={}", orderId, referenceId, quantity);

		return payment;
	}

	@Override
	public Payment saveFailedPayment(Long referenceId, String errorCode, String errorMessage, String orderId) {
		log.info("주문 결제 실패 정보 저장: referenceId={}, errorCode={}, errorMessage={}, orderId={}",
			referenceId, LogSanitizer.sanitizeLogInput(errorCode), LogSanitizer.sanitizeLogInput(errorMessage),
			orderId);

		ProductDetailResponse product = productService.getProductById(referenceId);
		String title = product.title();

		PaymentOrderInfo paymentOrderInfo = paymentQueryService.getPaymentOrderInfoByOrderId(orderId);
		Integer quantity = paymentOrderInfo.quantity();
		Member member = memberService.getMemberById(paymentOrderInfo.memberId());

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

package com.ll.dopdang.domain.payment.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import com.ll.dopdang.domain.payment.entity.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 주문 정보를 저장하기 위한 DTO 클래스
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private PaymentType paymentType;
	private Long referenceId;
	private String orderId;
	private Integer quantity;

	/**
	 * Map에서 PaymentOrderInfo 객체를 생성합니다.
	 *
	 * @param map 변환할 Map 객체
	 * @return PaymentOrderInfo 객체
	 */
	public static PaymentOrderInfo fromMap(Map<String, Object> map) {
		PaymentType paymentType = PaymentType.valueOf(map.get("paymentType").toString());
		Long referenceId = Long.valueOf(map.get("referenceId").toString());
		String orderId = (String)map.get("orderId");
		Integer quantity = map.get("quantity") != null ? Integer.valueOf(map.get("quantity").toString()) : null;

		return new PaymentOrderInfo(paymentType, referenceId, orderId, quantity);
	}
}

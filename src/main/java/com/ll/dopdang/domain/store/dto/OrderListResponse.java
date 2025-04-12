package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.ll.dopdang.domain.store.entity.DigitalContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderListResponse {
	private Long id;
	private String orderId;
	private String sellerName;
	private String productType;
	private String productThumbnail;
	private String productTitle;
	private String productPrice;
	private String quantity;
	private BigDecimal totalPrice;
	private String status;
	private String paymentMethod;
	private LocalDateTime orderedAt;
	private List<DigitalContent> digitalContent;

	public static OrderListResponse of(Map<String, Object> orderDetails, List<DigitalContent> digitalContents) {
		return OrderListResponse.builder()
			.id(((Number)orderDetails.get("id")).longValue())
			.orderId((String)orderDetails.get("order_number"))
			.sellerName((String)orderDetails.get("seller_name"))
			.productType((String)orderDetails.get("product_type"))
			.productThumbnail((String)orderDetails.get("product_thumbnail"))
			.productTitle((String)orderDetails.get("product_title"))
			.productPrice(orderDetails.get("product_price").toString())
			.quantity(orderDetails.get("quantity").toString())
			.totalPrice(new BigDecimal(orderDetails.get("total_amount").toString()))
			.status((String)orderDetails.get("status"))
			.paymentMethod((String)orderDetails.get("payment_method"))
			.orderedAt(((Timestamp)orderDetails.get("created_at")).toLocalDateTime())
			.digitalContent(digitalContents)
			.build();
	}
}
//
// {
//   "id" : Long,
//   "orderId" : String,
//   "sellerName" : String,
//   "productType" : enum,
//   "productThumbnail" : String,
//   "productTitle" : String,
//   "productPrice" : BigDecimal,
//   "quantity" : Long,
//   "totalPrice" : BigDecimal,
//   "status" : enum,
//   "paymentMethod" : String,
//   "orderedAt" : LocalDateTime,
//   "digitalContent" : [
//   	{
// 			"fileName": Sring,
// 			"fileUrl": String,
// 			"fileSize": Long,
// 			"fileType": String,
// 			"downloadLimit": Long
//   	},
//   ]
// }

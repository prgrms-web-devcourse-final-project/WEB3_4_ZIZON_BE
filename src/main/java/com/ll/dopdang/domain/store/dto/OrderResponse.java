package com.ll.dopdang.domain.store.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.ll.dopdang.domain.store.entity.DigitalContent;

public record OrderResponse(
	Long id,
	String orderId,
	String sellerName,
	String productType,
	String productThumbnail,
	String productTitle,
	String productPrice,
	String quantity,
	BigDecimal totalPrice,
	String status,
	String paymentMethod,
	LocalDateTime orderedAt,
	List<DigitalContent> digitalContent
) {
	public static OrderResponse of(Map<String, Object> orderDetails, List<DigitalContent> digitalContents) {
		return new OrderResponse(
			((Number)orderDetails.get("id")).longValue(),
			(String)orderDetails.get("order_number"),
			(String)orderDetails.get("seller_name"),
			(String)orderDetails.get("product_type"),
			(String)orderDetails.get("product_thumbnail"),
			(String)orderDetails.get("product_title"),
			orderDetails.get("product_price").toString(),
			orderDetails.get("quantity").toString(),
			new BigDecimal(orderDetails.get("total_amount").toString()),
			(String)orderDetails.get("status"),
			(String)orderDetails.get("payment_method"),
			((Timestamp)orderDetails.get("created_at")).toLocalDateTime(),
			digitalContents
		);
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

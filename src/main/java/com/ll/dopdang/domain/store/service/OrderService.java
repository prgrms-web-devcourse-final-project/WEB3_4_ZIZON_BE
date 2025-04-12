package com.ll.dopdang.domain.store.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.store.dto.DigitalContentProjection;
import com.ll.dopdang.domain.store.dto.OrderListPageResponse;
import com.ll.dopdang.domain.store.dto.OrderResponse;
import com.ll.dopdang.domain.store.entity.DigitalContent;
import com.ll.dopdang.domain.store.repository.OrderRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;

	@Transactional
	public OrderListPageResponse findMyPurchasedProducts(Pageable pageable, CustomUserDetails userDetails) {

		List<Map<String, Object>> orderDetailsList = orderRepository.findAllOrderDetailsWithProductByMemberId(
			userDetails.getId(), pageable);

		Long totalCount = orderRepository.countOrdersByMemberId(userDetails.getId());
		boolean hasNext = (pageable.getOffset() + pageable.getPageSize()) < totalCount;

		List<OrderResponse> orderResponses = orderDetailsList.stream()
			.map(orderDetails -> {
				Long orderId = ((Number)orderDetails.get("id")).longValue();
				List<DigitalContentProjection> digitalContentProjections = orderRepository
					.findAllDigitalContentByOrderId(orderId);
				List<DigitalContent> digitalContents = convertToDigitalContents(digitalContentProjections);
				return OrderResponse.of(orderDetails, digitalContents);
			}).toList();

		return OrderListPageResponse.of(
			orderResponses,
			pageable.getPageNumber(),
			pageable.getPageSize(),
			hasNext
		);
	}

	private List<DigitalContent> convertToDigitalContents(List<DigitalContentProjection> projections) {
		return projections.stream()
			.map(DigitalContent::from)
			.collect(Collectors.toList());
	}
}

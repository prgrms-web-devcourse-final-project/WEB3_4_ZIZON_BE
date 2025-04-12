package com.ll.dopdang.domain.store.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.service.MemberUtilService;
import com.ll.dopdang.domain.store.dto.DigitalContentProjection;
import com.ll.dopdang.domain.store.dto.OrderListPageResponse;
import com.ll.dopdang.domain.store.dto.OrderListResponse;
import com.ll.dopdang.domain.store.entity.DigitalContent;
import com.ll.dopdang.domain.store.repository.OrderRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final MemberUtilService memberUtilService;
	private final OrderRepository orderRepository;

	@Transactional
	public OrderListPageResponse findMyPurchasedProducts(Pageable pageable, CustomUserDetails userDetails) {

		List<Map<String, Object>> orderDetailsList = orderRepository.findAllOrderDetailsWithProductByMemberId(
			userDetails.getId(), pageable);

		Long totalCount = orderRepository.countOrdersByMemberId(userDetails.getId());
		boolean hasNext = (pageable.getOffset() + pageable.getPageSize()) < totalCount;

		List<OrderListResponse> orderListResponses = orderDetailsList.stream()
			.map(orderDetails -> {
				Long orderId = ((Number)orderDetails.get("id")).longValue();
				List<DigitalContentProjection> digitalContentProjections = orderRepository.findAllDigitalContentByOrderId(
					orderId);
				List<DigitalContent> digitalContents = convertToDigitalContents(digitalContentProjections);
				return OrderListResponse.of(orderDetails, digitalContents);
			}).toList();

		return OrderListPageResponse.builder()
			.orders(orderListResponses)
			.currentPage(pageable.getPageNumber())
			.pageSize(pageable.getPageSize())
			.hasNext(hasNext)
			.build();
	}

	private List<DigitalContent> convertToDigitalContents(List<DigitalContentProjection> projections) {
		return projections.stream().map(projection -> {
			DigitalContent content = new DigitalContent();

			return DigitalContent.builder()
				.id(projection.getId())
				.fileName(projection.getFileName())
				.fileUrl(projection.getFileUrl())
				.fileSize(projection.getFileSize())
				.fileType(projection.getFileType())
				.downloadLimit(projection.getDownloadLimit())
				.build();
		}).collect(Collectors.toList());
	}
}

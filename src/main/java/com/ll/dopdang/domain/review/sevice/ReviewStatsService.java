package com.ll.dopdang.domain.review.sevice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.review.dto.ReviewStatsResponse;
import com.ll.dopdang.domain.review.entity.ReviewStats;
import com.ll.dopdang.domain.review.repository.ReviewRepository;
import com.ll.dopdang.domain.review.repository.ReviewStatsRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewStatsService {

	private final ReviewRepository reviewRepository;
	private final ReviewStatsRepository reviewStatsRepository;

	@Transactional(readOnly = true)
	public ReviewStatsResponse getStatsByExpertId(Long expertId) {
		ReviewStats stats = reviewStatsRepository.findById(expertId)
			.orElseThrow(() -> new ServiceException(ErrorCode.REVIEW_STATS_NOT_FOUND));

		return ReviewStatsResponse.from(stats);
	}
}

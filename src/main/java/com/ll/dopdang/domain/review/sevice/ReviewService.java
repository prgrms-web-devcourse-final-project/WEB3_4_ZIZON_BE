package com.ll.dopdang.domain.review.sevice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.repository.ContractRepository;
import com.ll.dopdang.domain.review.dto.ReviewCreateRequest;
import com.ll.dopdang.domain.review.dto.ReviewCreateResponse;
import com.ll.dopdang.domain.review.entity.Review;
import com.ll.dopdang.domain.review.repository.ReviewRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ContractRepository contractRepository;
	private final MemberRepository memberRepository;
	private final ReviewRepository reviewRepository;

	/**
	 * 주어진 프로젝트 ID에 대해 리뷰를 생성합니다.
	 * 사용자는 자신이 클라이언트인 프로젝트에 대해,
	 * 계약이 완료(COMPLETED) 상태인 경우에만 리뷰를 작성할 수 있습니다.
	 * 이미 해당 계약에 리뷰가 존재할 경우 예외가 발생합니다.
	 *
	 * @param projectId 리뷰를 작성할 프로젝트의 ID
	 * @param reviewerId 리뷰 작성자 아이디 (현재 로그인된 사용자)
	 * @param request 리뷰 생성 요청 정보 (평점, 내용, 이미지 URL 등)
	 * @return 생성된 리뷰의 정보를 담은 응답 DTO
	 */
	@Transactional
	public ReviewCreateResponse createReview(Long projectId, Long reviewerId, ReviewCreateRequest request) {
		// 0. Member 조회
		Member reviewer = memberRepository.findById(reviewerId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

		// 1. 계약 조회
		Contract contract = contractRepository.findByProjectIdAndClientId(projectId, reviewer.getId())
			.orElseThrow(() -> new ServiceException(ErrorCode.CONTRACT_NOT_FOUND));

		// 2. 계약 상태 체크
		if (!contract.getStatus().equals(Contract.ContractStatus.COMPLETED)) {
			throw new ServiceException(ErrorCode.CONTRACT_NOT_COMPLETED);
		}

		// 3. 중복 체크
		if (reviewRepository.existsByContract(contract)) {
			throw new ServiceException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		// 4. 리뷰 생성
		Review review = Review.fromContract(
			contract,
			reviewer,
			request.getScore(),
			request.getContent(),
			request.getImageUrl());
		Review saved = reviewRepository.save(review);

		// 5. 응답 생성
		return ReviewCreateResponse.from(saved);
	}
}

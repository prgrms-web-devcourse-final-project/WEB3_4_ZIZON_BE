package com.ll.dopdang.domain.review.sevice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.repository.ContractRepository;
import com.ll.dopdang.domain.review.dto.ClientReviewPageResponse;
import com.ll.dopdang.domain.review.dto.ExpertReviewPageResponse;
import com.ll.dopdang.domain.review.dto.ReviewCreateRequest;
import com.ll.dopdang.domain.review.dto.ReviewCreateResponse;
import com.ll.dopdang.domain.review.dto.ReviewDetailResponse;
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

	/**
	 * 프로젝트 ID를 기반으로 리뷰 상세 정보를 조회합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @return 리뷰 상세 응답 DTO
	 * @throws ServiceException 리뷰 또는 계약이 존재하지 않을 경우 예외 발생
	 */
	@Transactional(readOnly = true)
	public ReviewDetailResponse getReviewByProjectId(Long projectId) {
		Contract contract = contractRepository.findByProjectId(projectId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CONTRACT_NOT_FOUND));

		Review review = reviewRepository.findByContract(contract)
			.orElseThrow(() -> new ServiceException(ErrorCode.REVIEW_NOT_FOUND));

		return ReviewDetailResponse.from(review);
	}

	/**
	 * 전문가가 받은 리뷰 목록 조회 (완료된 계약 기준)
	 *
	 * @param expertId 전문가 ID
	 * @param pageable 페이징 정보
	 * @return 페이지 형태의 리뷰 응답
	 */
	public ExpertReviewPageResponse getReviewsByExpert(Long expertId, Pageable pageable) {
		Page<Review> page = reviewRepository.findByExpertIdWithReviewer(expertId, pageable);
		return ExpertReviewPageResponse.from(page);
	}

	/**
	 * 특정 클라이언트가 작성한 리뷰 목록 조회
	 * - 계약이 완료된 리뷰만 조회
	 * - 무한 스크롤(오프셋 기반) 방식 지원
	 * - 전문가 이름 및 프로필 이미지 포함
	 *
	 * @param clientId 클라이언트(작성자) ID
	 * @param pageable 페이징 정보 (page, size, sort)
	 * @return 클라이언트 리뷰 목록 페이지 응답
	 */
	public ClientReviewPageResponse getClientReviews(Long clientId, Pageable pageable) {
		Page<Review> page = reviewRepository.findByReviewerIdAndCompletedContract(clientId, pageable);
		return ClientReviewPageResponse.from(page);
	}
}

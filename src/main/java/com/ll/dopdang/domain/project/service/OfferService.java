package com.ll.dopdang.domain.project.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.project.dto.OfferCreateRequest;
import com.ll.dopdang.domain.project.dto.OfferDetailResponse;
import com.ll.dopdang.domain.project.entity.Offer;
import com.ll.dopdang.domain.project.entity.Project;
import com.ll.dopdang.domain.project.entity.ProjectStatus;
import com.ll.dopdang.domain.project.repository.OfferRepository;
import com.ll.dopdang.domain.project.repository.ProjectRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
	private final ExpertRepository expertRepository;
	private final ProjectRepository projectRepository;
	private final OfferRepository offerRepository;

	/**
	 * 전문가가 특정 프로젝트에 오퍼를 생성합니다.
	 *
	 * @param userDetails 로그인한 사용자 정보
	 * @param projectId 오퍼를 보낼 프로젝트 ID
	 * @param request 오퍼 생성 요청 정보 (가격, 작업일 등)
	 * @throws ServiceException 전문가가 아니거나 프로젝트를 찾을 수 없는 경우
	 */
	public void createOffer(CustomUserDetails userDetails, Long projectId, OfferCreateRequest request) {
		Expert expert = expertRepository.findByMemberId(userDetails.getId()).orElseThrow(
			() -> new ServiceException(ErrorCode.NOT_A_EXPERT_USER));

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PROJECT_NOT_FOUND));

		boolean exists = offerRepository.existsByProjectAndExpert(project, expert);
		if (exists) {
			throw new ServiceException(ErrorCode.OFFER_ALREADY_EXISTS);
		}

		Offer offer = Offer.builder()
			.expert(expert)
			.project(project)
			.price(request.getPrice())
			.deliveryDays(request.getDeliveryDays())
			.status(Offer.OfferStatus.PENDING)
			.build();
		offerRepository.save(offer);
		project.updateStatus(ProjectStatus.IN_PROGRESS);
	}

	/**
	 * 오퍼 상세 정보를 조회합니다.
	 * 클라이언트(의뢰자) 또는 전문가(작성자)만 조회할 수 있습니다.
	 *
	 * @param userDetails 로그인한 사용자 정보
	 * @param projectId 프로젝트 ID
	 * @param offerId 오퍼 ID
	 * @return 오퍼 상세 응답 DTO
	 */
	public OfferDetailResponse getOfferById(CustomUserDetails userDetails, Long projectId, Long offerId) {
		Project project = projectRepository.findById(projectId).orElseThrow(
			() -> new ServiceException(ErrorCode.PROJECT_NOT_FOUND));

		Offer offer = offerRepository.findById(offerId).orElseThrow(
			() -> new ServiceException(ErrorCode.OFFER_NOT_FOUND));

		if (!offer.getProject().getId().equals(projectId)) {
			throw new ServiceException(ErrorCode.INVALID_OFFER_PROJECT);
		}

		Long userId = userDetails.getId();
		Long clientId = project.getClient().getId();
		Long expertMemberId = offer.getExpert().getMember().getId();

		log.info("로그인한 유저 ID: {}", userId);
		log.info("프로젝트 클라이언트 ID: {}", clientId);
		log.info("오퍼 전문가 멤버 ID: {}", expertMemberId);

		boolean isClient = clientId.equals(userId);
		boolean isExpert = expertMemberId.equals(userId);

		if (!isClient && !isExpert) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}

		return new OfferDetailResponse(offer);
	}

	/**
	 * 오퍼 엔티티를 저장합니다.
	 *
	 * @param offer 저장할 오퍼 엔티티
	 * @return 저장된 오퍼 엔티티
	 */
	public Offer saveOffer(Offer offer) {
		return offerRepository.save(offer);
	}

	/**
	 * 같은 프로젝트의 다른 오퍼들을 거절 상태로 변경합니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param acceptedOfferId 수락된 오퍼 ID
	 */
	@Transactional
	public void rejectOtherOffers(Long projectId, Long acceptedOfferId) {
		List<Offer> otherOffers = offerRepository.findByProjectIdAndIdNot(projectId, acceptedOfferId);

		for (Offer offer : otherOffers) {
			// PENDING 상태인 오퍼만 REJECTED로 변경
			if (offer.getStatus() == Offer.OfferStatus.PENDING) {
				offer.setStatus(Offer.OfferStatus.REJECTED);
				offerRepository.save(offer);
			}
		}
	}

	/**
	 * 오퍼 ID로 오퍼 엔티티를 조회합니다.
	 * 내부 서비스용 (검증 없음)
	 *
	 * @param offerId 오퍼 ID
	 * @return 오퍼 엔티티
	 * @throws ServiceException 오퍼를 찾을 수 없는 경우
	 */
	public Offer getOfferById(Long offerId) {
		return offerRepository.findById(offerId)
			.orElseThrow(() -> new ServiceException(ErrorCode.OFFER_NOT_FOUND));
	}

	/**
	 * 프로젝트 ID와 전문가 ID를 기반으로 오퍼를 조회합니다.
	 * 클라이언트만 접근 가능하도록 권한 체크 포함.
	 *
	 * @param projectId 프로젝트 ID
	 * @param expertId 전문가 ID
	 * @param userDetails 로그인한 사용자 정보
	 * @return 오퍼 엔티티
	 */
	public Offer getOfferByProjectAndExpert(Long projectId, Long expertId, CustomUserDetails userDetails) {
		log.info("userDetails.getId(): {}", userDetails.getId());
		if (userDetails == null) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}
		Offer offer = offerRepository.findByProjectIdAndExpertId(projectId, expertId)
			.orElseThrow(() -> new ServiceException(ErrorCode.OFFER_NOT_FOUND));


		Long loggedInUserId = userDetails.getId();
		Long clientId = offer.getProject().getClient().getId();
		Long expertMemberId = offer.getExpert().getMember().getId();

		// 클라이언트도 아니고, 전문가 본인도 아니라면 예외
		if (!clientId.equals(loggedInUserId) && !expertMemberId.equals(loggedInUserId)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_USER);
		}

		return offer;
	}
}

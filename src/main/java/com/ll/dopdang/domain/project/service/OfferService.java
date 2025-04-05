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

	public void createOffer(CustomUserDetails userDetails, Long projectId, OfferCreateRequest request) {
		Expert expert = expertRepository.findByMemberId(userDetails.getId()).orElseThrow(
			() -> new ServiceException(ErrorCode.NOT_A_EXPERT_USER));

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PROJECT_NOT_FOUND));

		Offer offer = Offer.builder()
			.expert(expert)
			.project(project)
			.price(request.getPrice())
			.deliveryDays(request.getDeliveryDays())
			.status(Offer.OfferStatus.PENDING)
			.build();
		offerRepository.save(offer);
	}

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

	public Offer getOfferById(Long offerId) {
		return offerRepository.findById(offerId)
			.orElseThrow(() -> new ServiceException(ErrorCode.OFFER_NOT_FOUND));
	}
}

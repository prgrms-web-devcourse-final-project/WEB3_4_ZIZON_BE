package com.ll.dopdang.domain.project.service;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.project.entity.Offer;
import com.ll.dopdang.domain.project.repository.OfferRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferService {

	private final OfferRepository offerRepository;

	/**
	 * 오퍼 ID로 오퍼를 조회합니다.
	 *
	 * @param offerId 조회할 오퍼 ID
	 * @return 조회된 오퍼 엔티티
	 * @throws ServiceException 오퍼를 찾을 수 없는 경우
	 */
	public Offer getOfferById(Long offerId) {
		return offerRepository.findById(offerId)
			.orElseThrow(
				() -> new ServiceException(ErrorCode.INVALID_INPUT_VALUE, "오퍼 ID: " + offerId + "를 찾을 수 없습니다."));
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
}

package com.ll.dopdang.domain.project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.entity.Offer;
import com.ll.dopdang.domain.project.repository.ContractRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractService {

	private final OfferService offerService;
	private final ContractRepository contractRepository;

	public Contract getContractById(Long contractId) {
		return contractRepository.findById(contractId)
			.orElseThrow(
				() -> new ServiceException(ErrorCode.CONTRACT_NOT_FOUND, "계약 ID: " + contractId + "를 찾을 수 없습니다."));
	}

	@Transactional
	public Long createContractFromOffer(Long offerId, BigDecimal price, LocalDateTime startDate,
		LocalDateTime endDate) {
		// 1. 오퍼 ID로 오퍼 찾기
		Offer offer = offerService.getOfferById(offerId);

		// 1-1. 오퍼 상태 확인 - PENDING 상태가 아니면 이미 처리된 오퍼
		if (offer.getStatus() != Offer.OfferStatus.PENDING) {
			throw new ServiceException(ErrorCode.OFFER_ALREADY_PROCESSED,
				"이미 처리된 오퍼입니다. 오퍼 ID: " + offerId + ", 현재 상태: " + offer.getStatus());
		}

		// 2. 오퍼와 제공된 세부 정보를 기반으로 새 계약 생성
		Contract contract = Contract.createFromOffer(offer, price, startDate, endDate);

		// 3. 계약 저장
		Contract savedContract = contractRepository.save(contract);

		// 4. 오퍼 상태를 ACCEPTED로 업데이트
		offer.setStatus(Offer.OfferStatus.ACCEPTED);
		offerService.saveOffer(offer);

		// 5. 계약 ID 반환
		return savedContract.getId();
	}
}

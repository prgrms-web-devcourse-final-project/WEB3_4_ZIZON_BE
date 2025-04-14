package com.ll.dopdang.domain.project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.project.dto.ContractDetailResponse;
import com.ll.dopdang.domain.project.dto.ContractSummaryResponse;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.project.entity.Offer;
import com.ll.dopdang.domain.project.entity.Project;
import com.ll.dopdang.domain.project.entity.ProjectStatus;
import com.ll.dopdang.domain.project.repository.ContractRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractService {

	private final OfferService offerService;
	private final ContractRepository contractRepository;

	/**
	 * 계약 ID로 계약 단건 조회
	 *
	 * @param contractId 계약 ID
	 * @return 계약 엔티티
	 */
	public Contract getContractById(Long contractId) {
		return contractRepository.findById(contractId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CONTRACT_NOT_FOUND,
				"계약 ID: " + contractId + "를 찾을 수 없습니다."));
	}

	/**
	 * 오퍼 정보를 바탕으로 계약을 생성
	 * - 오퍼 상태 확인 (PENDING)
	 * - 계약 저장 및 오퍼 상태 업데이트 (ACCEPTED)
	 * - 동일 프로젝트의 다른 오퍼는 REJECTED 처리
	 *
	 * @param offerId 오퍼 ID
	 * @param price 계약 금액
	 * @param startDate 계약 시작일
	 * @param endDate 계약 종료일
	 * @return 생성된 계약 ID
	 */
	@Transactional
	public Long createContractFromOffer(Long offerId, BigDecimal price, LocalDateTime startDate,
		LocalDateTime endDate) {

		// 1. 오퍼 조회
		Offer offer = offerService.getOfferById(offerId);

		// 2. 오퍼 상태 확인 (PENDING만 계약 가능)
		if (offer.getStatus() != Offer.OfferStatus.PENDING) {
			throw new ServiceException(ErrorCode.OFFER_ALREADY_PROCESSED,
				"이미 처리된 오퍼입니다. 오퍼 ID: " + offerId + ", 현재 상태: " + offer.getStatus());
		}

		// 3. 오퍼 기반 계약 생성
		Contract contract = Contract.createFromOffer(offer, price, startDate, endDate);

		// 4. 계약 저장
		Contract savedContract = contractRepository.save(contract);

		// 5. 해당 오퍼 상태 ACCEPTED로 업데이트
		offer.setStatus(Offer.OfferStatus.ACCEPTED);
		offerService.saveOffer(offer);

		// 6. 동일 프로젝트의 다른 오퍼는 모두 REJECTED 처리
		Long projectId = offer.getProject().getId();
		offerService.rejectOtherOffers(projectId, offerId);

		// 7. 프로젝트 상태 변경
		Project project = offer.getProject();
		project.updateStatus(ProjectStatus.IN_PROGRESS);

		// 8. 생성된 계약 ID 반환
		return savedContract.getId();
	}

	/**
	 * 전문가의 계약 목록 조회 (무한 스크롤 지원)
	 *
	 * @param expertMemberId 로그인한 전문가의 memberId
	 * @param pageable 오프셋 기반 페이지 정보
	 * @return 계약 요약 정보 리스트
	 */
	public List<ContractSummaryResponse> getContractsForExpert(Long expertMemberId, Pageable pageable) {
		List<Contract> contracts = contractRepository.findContractsByExpertMemberId(expertMemberId, pageable);

		return contracts.stream()
			.map(contract -> ContractSummaryResponse.builder()
				.contractId(contract.getId())
				.projectId(contract.getProject().getId())
				.projectTitle(contract.getProject().getTitle())
				.clientName(contract.getClient().getName())
				.price(contract.getPrice())
				.startDate(contract.getStartDate())
				.endDate(contract.getEndDate())
				.status(contract.getStatus().name())
				.build())
			.toList();
	}

	/**
	 * 프로젝트 ID로 계약 단건 조회 (존재하지 않으면 예외 발생)
	 *
	 * @param projectId 프로젝트 ID
	 * @return 계약 엔티티
	 */
	public Contract findByProjectIdOrElseThrow(Long projectId) {
		return contractRepository.findByProjectId(projectId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CONTRACT_NOT_FOUND));
	}

	/**
	 * 계약 상세 조회
	 * - 계약은 클라이언트 혹은 전문가 본인만 조회 가능
	 *
	 * @param projectId 프로젝트 ID
	 * @param loginMember 로그인한 사용자
	 * @return 계약 상세 응답 DTO
	 */
	public ContractDetailResponse getContractDetail(Long projectId, Member loginMember) {
		Contract contract = findByProjectIdOrElseThrow(projectId);

		// 1. 권한 확인 (클라이언트 또는 전문가 본인)
		boolean isClient = loginMember.getId().equals(contract.getClient().getId());
		boolean isExpert = loginMember.getId().equals(contract.getExpert().getMember().getId());

		if (!isClient && !isExpert) {
			throw new ServiceException(ErrorCode.CONTRACT_ACCESS_DENIED);
		}

		// 2. DTO 변환 후 응답
		return ContractDetailResponse.from(contract);
	}

	@Transactional
	public void updateContractStatusToAsCompleted(Long contractId, Long clientId) {
		// 1. 계약 조회 (프로젝트, 클라이언트 fetch join)
		Contract contract = contractRepository.findByIdWithProjectAndClient(contractId)
			.orElseThrow(() -> new ServiceException(ErrorCode.CONTRACT_NOT_FOUND));

		// 2. 클라이언트 소유 검증
		if (!contract.getClient().getId().equals(clientId)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED_CONTRACT_ACCESS);
		}

		// 3. 이미 완료된 상태인지 확인
		if (contract.getStatus() == Contract.ContractStatus.COMPLETED) {
			throw new ServiceException(ErrorCode.CONTRACT_ALREADY_COMPLETED);
		}

		// 4. 상태 변경
		contract.updateStatus(Contract.ContractStatus.COMPLETED);

		// 5. 프로젝트 상태 변경
		Project project = contract.getProject();
		project.updateStatus(ProjectStatus.COMPLETED);
	}
}

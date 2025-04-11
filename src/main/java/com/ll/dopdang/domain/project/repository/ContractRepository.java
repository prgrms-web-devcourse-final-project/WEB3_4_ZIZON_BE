package com.ll.dopdang.domain.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.project.entity.Contract;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

	/**
	 * 특정 전문가의 계약 목록을 조회합니다.
	 * 해당 전문가(Member ID 기준)가 속한 계약들을 생성일 기준으로 내림차순 정렬하여 반환합니다.
	 *
	 * @param memberId 전문가의 Member ID
	 * @param pageable 페이징 정보
	 * @return 전문가가 받은 계약 목록
	 */
	@Query("SELECT c FROM Contract c "
		+ "JOIN FETCH c.project p "
		+ "JOIN FETCH c.client m "
		+ "WHERE c.expert.member.id = :memberId "
		+ "ORDER BY c.createdAt DESC")
	List<Contract> findContractsByExpertMemberId(@Param("memberId") Long memberId, Pageable pageable);

	/**
	 * 계약 ID로 계약을 조회하면서, 해당 계약에 연결된 프로젝트와 클라이언트 정보도 함께 가져옵니다.
	 * 주로 계약 상태를 변경하거나 검증할 때 사용됩니다.
	 *
	 * @param contractId 계약 ID
	 * @return 프로젝트 및 클라이언트 정보가 포함된 계약
	 */
	@Query("SELECT c FROM Contract c "
		+ "JOIN FETCH c.project p "
		+ "JOIN FETCH c.client cl "
		+ "WHERE c.id = :contractId")
	Optional<Contract> findByIdWithProjectAndClient(@Param("contractId") Long contractId);

	/**
	 * 프로젝트 ID를 기준으로 계약을 조회합니다.
	 * 프로젝트에 연결된 계약이 있는지 여부를 확인하거나, 계약 정보를 가져올 때 사용됩니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @return 해당 프로젝트에 연결된 계약 (있다면)
	 */
	Optional<Contract> findByProjectId(Long projectId);

	/**
	 * 특정 프로젝트 ID와 클라이언트 ID를 기반으로 계약을 조회합니다.
	 * 사용자가 자신의 프로젝트에 대한 계약이 존재하는지 확인할 때 주로 사용됩니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param clientId 클라이언트 ID
	 * @return 해당 프로젝트-클라이언트 조합의 계약 (있다면)
	 */
	Optional<Contract> findByProjectIdAndClientId(Long projectId, Long clientId);
}

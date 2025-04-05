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

	@Query("SELECT c FROM Contract c "
		+ "JOIN FETCH c.project p "
		+ "JOIN FETCH c.client m "
		+ "WHERE c.expert.member.id = :memberId "
		+ "ORDER BY c.createdAt DESC")
	List<Contract> findContractsByExpertMemberId(@Param("memberId") Long memberId, Pageable pageable);

	@Query("SELECT c FROM Contract c WHERE c.project.id = :projectId")
	Optional<Contract> findByProjectId(@Param("projectId") Long projectId);
}

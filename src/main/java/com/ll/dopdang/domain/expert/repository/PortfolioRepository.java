package com.ll.dopdang.domain.expert.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.Portfolio;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
	// 특정 전문가의 포트폴리오 조회
	Optional<Portfolio> findByExpertId(Long expertId);

	@Modifying
	@Query("DELETE FROM Portfolio ec WHERE ec.expert.id = :expertId")
	void deleteAllByExpertId(@Param("expertId") Long expertId);

	// 특정 전문가의 포트폴리오 유무 확인
	boolean existsByExpertId(Long expertId);
}
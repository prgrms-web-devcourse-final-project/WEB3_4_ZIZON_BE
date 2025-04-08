package com.ll.dopdang.domain.expert.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.Portfolio;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
	// 특정 전문가의 포트폴리오 조회
	Optional<Portfolio> findByExpertId(Long expertId);

	// 특정 전문가의 포트폴리오 유무 확인
	boolean existsByExpertId(Long expertId);
}
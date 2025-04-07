package com.ll.dopdang.domain.expert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.ExpertCertificate;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ExpertCertificateRepository extends JpaRepository<ExpertCertificate, Long> {
	@Modifying
	@Query("DELETE FROM ExpertCertificate ec WHERE ec.expert.id = :expertId")
	void deleteAllByExpertId(@Param("expertId") Long expertId);
}
package com.ll.dopdang.domain.expert.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.Certificate;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
	boolean existsByName(String name);

	@Query("SELECT c FROM Certificate c WHERE c.name LIKE %:name%")
	List<Certificate> findByNameContaining(@Param("name") String name);

	@Modifying
	@Query("DELETE FROM ExpertCategory ec WHERE ec.expert.id = :expertId")
	void deleteAllByExpertId(@Param("expertId") Long expertId);

	Optional<Certificate> findByName(String name);
}

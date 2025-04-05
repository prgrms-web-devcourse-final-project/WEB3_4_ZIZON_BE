package com.ll.dopdang.domain.expert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.Certificate;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
	boolean existsByName(String name);
}

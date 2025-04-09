package com.ll.dopdang.domain.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.store.entity.DigitalContent;

@Repository
public interface DigitalContentRepository extends JpaRepository<DigitalContent, Long> {

	/**
	 * 상품 ID로 디지털 콘텐츠 목록 조회
	 * @param productId 상품 ID
	 * @return 디지털 콘텐츠 목록
	 */
	List<DigitalContent> findAllByProductId(Long productId);
}

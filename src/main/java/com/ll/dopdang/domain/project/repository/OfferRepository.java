package com.ll.dopdang.domain.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.project.entity.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

	/**
	 * 프로젝트 ID와 오퍼 ID가 일치하지 않는 모든 오퍼를 찾습니다.
	 *
	 * @param projectId 프로젝트 ID
	 * @param offerId 제외할 오퍼 ID
	 * @return 해당 프로젝트의 특정 오퍼를 제외한 모든 오퍼 목록
	 */
	List<Offer> findByProjectIdAndIdNot(Long projectId, Long offerId);
}

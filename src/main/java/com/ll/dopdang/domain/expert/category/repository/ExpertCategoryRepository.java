package com.ll.dopdang.domain.expert.category.repository;

import com.ll.dopdang.domain.expert.category.entity.ExpertCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpertCategoryRepository extends JpaRepository<ExpertCategory, Long> {
	List<ExpertCategory> findByExpertId(Long expertId);

	// 특정 전문가와 연결된 소분류 삭제
	void deleteAllByExpertId(Long expertId);
}

package com.ll.dopdang.domain.expert.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.category.entity.ExpertCategory;

@Repository
public interface ExpertCategoryRepository extends JpaRepository<ExpertCategory, Long> {
	List<ExpertCategory> findByExpertId(Long expertId);

	// 특정 전문가와 연결된 소분류 삭제
	@Modifying
	@Query("DELETE FROM ExpertCategory ec WHERE ec.expert.id = :expertId")
	void deleteAllByExpertId(Long expertId);
}

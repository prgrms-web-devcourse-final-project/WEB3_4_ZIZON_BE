package com.ll.dopdang.domain.category.repository;

import com.ll.dopdang.domain.category.entity.ExpertCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpertCategoryRepository extends JpaRepository<ExpertCategory, Long> {
    List<ExpertCategory> findByExpertId(Long expertId);
}

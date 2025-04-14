package com.ll.dopdang.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.review.entity.ReviewStats;

@Repository
public interface ReviewStatsRepository extends JpaRepository<ReviewStats, Long> {
	@Query("""
       SELECT rs
       FROM ReviewStats rs
       JOIN FETCH rs.expert e
       WHERE (:categoryId IS NULL OR e.category.id = :categoryId)
       ORDER BY rs.averageScore DESC, rs.reviewCount DESC
    """)
	List<ReviewStats> findTopExpertsByRatingAndCategory(@Param("categoryId") Long categoryId);
}

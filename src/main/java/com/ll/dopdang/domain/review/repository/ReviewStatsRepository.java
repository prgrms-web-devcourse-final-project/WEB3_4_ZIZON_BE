package com.ll.dopdang.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.review.entity.ReviewStats;

public interface ReviewStatsRepository extends JpaRepository<ReviewStats, Long> {

}

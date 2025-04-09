package com.ll.dopdang.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	boolean existsByContract(Contract contract);
	// 필요에 따라 커스텀 쿼리 메서드 추가 가능
}

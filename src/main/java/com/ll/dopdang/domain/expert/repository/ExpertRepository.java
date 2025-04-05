package com.ll.dopdang.domain.expert.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.Expert;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
	@NotNull Optional<Expert> findByMemberId(@NotNull Long memberId);
}

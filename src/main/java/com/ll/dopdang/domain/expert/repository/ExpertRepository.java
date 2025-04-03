package com.ll.dopdang.domain.expert.repository;

import com.ll.dopdang.domain.expert.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import com.ll.dopdang.domain.expert.entity.Expert;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    @Query("SELECT e FROM Expert e JOIN FETCH e.member")
    List<Expert> findAllWithMember();

    Optional<Expert> findByMemberId(Long memberId);
}

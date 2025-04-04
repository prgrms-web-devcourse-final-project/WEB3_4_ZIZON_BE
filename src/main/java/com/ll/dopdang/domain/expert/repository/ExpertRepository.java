package com.ll.dopdang.domain.expert.repository;

import com.ll.dopdang.domain.expert.entity.Expert;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    @Query("SELECT e FROM Expert e JOIN FETCH e.member")
    List<Expert> findAllWithMember();

    @Query("SELECT e FROM Expert e WHERE "
            + "(e.careerYears >= :minYears OR :minYears IS NULL) AND "
            + "(e.careerYears <= :maxYears OR :maxYears IS NULL) AND "
            + "(e.mainCategory.name IN :categoryNames OR :categoryNames IS NULL)")
    List<Expert> findByFilters(@Param("categoryNames") List<String> categoryNames,
                               @Param("minYears") Integer minYears,
                               @Param("maxYears") Integer maxYears);


    Optional<Expert> findByMemberId(Long memberId);
}

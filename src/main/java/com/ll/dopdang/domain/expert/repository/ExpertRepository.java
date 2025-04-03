package com.ll.dopdang.domain.expert.repository;

import com.ll.dopdang.domain.expert.entity.Expert;
import io.lettuce.core.dynamic.annotation.Param;
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

    @Query("SELECT e FROM Expert e " +
            "WHERE (:categoryNames IS NULL OR e.mainCategory.name IN :categoryNames) " +
            "AND (:minYears IS NULL OR e.careerYears >= :minYears) " +
            "AND (:maxYears IS NULL OR e.careerYears <= :maxYears)")
    List<Expert> findByFilters(@Param("categoryNames") List<String> categoryNames,
                               @Param("minYears") Integer minYears,
                               @Param("maxYears") Integer maxYears);

    Optional<Expert> findByMemberId(Long memberId);
}

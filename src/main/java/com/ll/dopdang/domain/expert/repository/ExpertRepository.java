package com.ll.dopdang.domain.expert.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.expert.entity.Expert;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {

    @Query("SELECT e FROM Expert e JOIN FETCH e.member")
    List<Expert> findAllWithMember();


    @Query("SELECT e FROM Expert e WHERE e.id = :id AND e.availability = true")
    Optional<Expert> findAvailableById(@Param("id") Long id);


    // 카테고리, 경력 필터링을 포함한 조회
    @Query("""
        SELECT e
        FROM Expert e
        WHERE e.availability = true
        AND (:categories IS NULL OR e.category.name IN :categories)
        AND (:minYears IS NULL OR e.careerYears >= :minYears)
        AND (:maxYears IS NULL OR e.careerYears <= :maxYears)
        """)
    List<Expert> findAvailableByFilters(
        @Param("categories") List<String> categories,
        @Param("minYears") Integer minYears,
        @Param("maxYears") Integer maxYears
    );

    @Modifying
    @Query("UPDATE Expert e SET e.availability = false WHERE e.id = :expertId")
    void softDelete(@Param("expertId") Long expertId);

    Optional<Expert> findByMemberId(Long memberId);

    @Query("SELECT e FROM Expert e WHERE LOWER(e.member.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Expert> findByMemberNameContaining(@Param("name") String name);

    @Query("SELECT e FROM Expert e WHERE e.availability = true AND e.member.name LIKE %:name%")
    List<Expert> findAvailableByMemberNameContaining(@Param("name") String name);
}

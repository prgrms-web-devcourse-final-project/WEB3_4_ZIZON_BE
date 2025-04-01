package com.ll.dopdang.domain.category.repository;

import com.ll.dopdang.domain.category.entity.Category;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findTopByLevelOrderByIdDesc(int level);

    Optional<Category> findTopByParentIdOrderByIdDesc(Integer parentId);

    List<Category> findAllByParentId(Integer parentId);

    @Query("SELECT MAX(c.id) FROM Category c WHERE c.parent IS NULL")
    Integer findMaxParentCategoryId();

    @Query("SELECT MAX(c.id) FROM Category c WHERE c.parent.id = :parentId")
    Integer findMaxChildCategoryId(@Param("parentId") Integer parentId);
}

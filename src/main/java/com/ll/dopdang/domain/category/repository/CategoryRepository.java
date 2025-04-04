package com.ll.dopdang.domain.category.repository;

import com.ll.dopdang.domain.category.entity.Category;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String name);
    // 대분류 카테고리를 이름으로 조회
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.parent IS NULL")
    Optional<Category> findByNameAndParentIsNull(@Param("name") String name);

    // 소분류 카테고리를 대분류와 이름으로 조회
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.parent = :parent")
    Optional<Category> findByNameAndParent(@Param("name") String name, @Param("parent") Category parent);
}
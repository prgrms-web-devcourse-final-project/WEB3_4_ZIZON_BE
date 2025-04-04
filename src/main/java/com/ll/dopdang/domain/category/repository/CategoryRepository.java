package com.ll.dopdang.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

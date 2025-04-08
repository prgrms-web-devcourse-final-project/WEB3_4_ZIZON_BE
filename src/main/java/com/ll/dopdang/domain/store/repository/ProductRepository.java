package com.ll.dopdang.domain.store.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ll.dopdang.domain.store.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findById(Long id);

	@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
	Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);
}

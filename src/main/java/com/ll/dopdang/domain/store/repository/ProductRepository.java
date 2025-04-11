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

	@Query(value = "SELECT * FROM product WHERE category_id = :categoryId " +
		"ORDER BY CASE WHEN stock > 0 THEN 0 ELSE 1 END",
		nativeQuery = true)
	Page<Product> findAllByCategoryOrderByStockAndCreatedAtDesc(Long categoryId, Pageable pageable);

	@Query(value = "SELECT * FROM product " +
		"ORDER BY CASE WHEN stock > 0 THEN 0 ELSE 1 END",
		nativeQuery = true)
	Page<Product> findAll(Pageable pageable);
}

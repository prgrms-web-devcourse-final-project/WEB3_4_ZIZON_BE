package com.ll.dopdang.domain.store.controller;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.store.dto.ProductCreateRequest;
import com.ll.dopdang.domain.store.dto.ProductListPageResponse;
import com.ll.dopdang.domain.store.dto.ProductUpdateRequest;
import com.ll.dopdang.domain.store.service.ProductService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Store", description = "Store API")
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	/**
	 * 제품을 등록하는 API
	 * @param request ProductCreateRequest
	 * @param userDetails 인증된 유저 정보
	 * @return {@link ResponseEntity}
	 */
	@Operation(summary = "제품 등록")
	@PostMapping
	public ResponseEntity<?> createProduct(
		@Valid @RequestBody ProductCreateRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		productService.createProduct(request, userDetails);

		return ResponseEntity.ok().body(Map.of("message", "제품이 성공적으로 등록되었습니다."));
	}

	/**
	 * 제품 다건 조회 API
	 * @param categoryId 카테고리 고유 ID
	 * @param keyword 검색 키워드
	 * @param pageable Pageable
	 * @return {@link ResponseEntity}
	 */
	@Operation(summary = "제품 다건 조회")
	@GetMapping
	public ResponseEntity<?> getAllProducts(
		@RequestParam(required = false) Long categoryId, // 선택한 제품 타입
		@RequestParam(required = false) String keyword, // 검색 키워드
		@PageableDefault(size = 10, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable) {

		ProductListPageResponse response = productService.getAllProducts(pageable, categoryId, keyword);

		return ResponseEntity.ok(response);
	}

	/**
	 * 제품의 단건 조회 API
	 * @param productId 제품의 고유 ID
	 * @return {@link ResponseEntity}
	 */
	@Operation(summary = "제품 단건 조회")
	@GetMapping("/{product_id}")
	public ResponseEntity<?> getProductById(
		@PathVariable("product_id") Long productId) {

		return ResponseEntity.ok(productService.getProductById(productId));
	}

	/**
	 * 제품의 정보를 수정하는 API
	 * @param productId 제품의 고유 ID
	 * @param userDetails 인증된 유저 정보
	 * @return {@link ResponseEntity}
	 */
	@Operation(summary = "제품 수정")
	@PatchMapping("/{product_id}")
	public ResponseEntity<?> updateProductById(
		@PathVariable("product_id") Long productId,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody ProductUpdateRequest request) {

		productService.updateProduct(request, productId, userDetails);

		return ResponseEntity.ok().body(Map.of("message", "제품이 성공적으로 수정되었습니다."));
	}

	/**
	 * 제품을 삭제하는 API
	 * @param productId 제품의 고유 ID
	 * @param userDetails 인증된 유저 정보
	 * @return {@link ResponseEntity}
	 */
	@Operation(summary = "제품 삭제")
	@DeleteMapping("/{product_id}")
	public ResponseEntity<?> deleteProductById(
		@PathVariable("product_id") Long productId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {

		productService.deleteProduct(productId, userDetails);

		return ResponseEntity.ok().body(Map.of("message", "제품이 성공적으로 삭제되었습니다."));
	}
}

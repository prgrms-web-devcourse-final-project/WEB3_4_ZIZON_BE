package com.ll.dopdang.domain.store.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.expert.category.entity.Category;
import com.ll.dopdang.domain.expert.category.service.CategoryService;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberUtilService;
import com.ll.dopdang.domain.store.dto.DigitalContentDetailResponse;
import com.ll.dopdang.domain.store.dto.ProductCreateRequest;
import com.ll.dopdang.domain.store.dto.ProductDetailResponse;
import com.ll.dopdang.domain.store.dto.ProductListPageResponse;
import com.ll.dopdang.domain.store.dto.ProductListResponse;
import com.ll.dopdang.domain.store.dto.ProductUpdateRequest;
import com.ll.dopdang.domain.store.entity.DigitalContent;
import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.entity.ProductType;
import com.ll.dopdang.domain.store.repository.DigitalContentRepository;
import com.ll.dopdang.domain.store.repository.ProductRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import io.jsonwebtoken.lang.Objects;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	public final MemberUtilService memberUtilService;
	private final ProductRepository productRepository;
	private final CategoryService categoryService;
	private final DigitalContentRepository digitalContentRepository;

	/**
	 * 상품 생성 메서드
	 * @param request ProductCreateRequest
	 * @param userDetails 인증된 유저 정보
	 */
	@Transactional
	public void createProduct(ProductCreateRequest request, CustomUserDetails userDetails) {
		Member member = memberUtilService.findMember(userDetails.getId());
		Expert expert = memberUtilService.validateExpert(member);
		Category category = categoryService.findById(request.getCategoryId());
		validateDigitalProduct(request);
		Product product = Product.from(request, category, expert);
		productRepository.save(product);
	}

	/**
	 * 상품 전체 조회 메서드
	 * @param pageable Pageable
	 * @param categoryId 카테고리 고유 ID
	 * @param keyword 검색 키워드
	 * return {@link ProductListPageResponse}
	 */
	public ProductListPageResponse getAllProducts(Pageable pageable, Long categoryId, String keyword) {
		Page<Product> page;

		if (categoryId != null && keyword != null && !keyword.isEmpty()) {
			// 카테고리와 키워드로 검색
			page = productRepository.findAllByCategoryAndKeyword(categoryId, keyword, pageable);
		} else if (categoryId != null) {
			// 카테고리로만 검색
			page = productRepository.findAllByCategoryOrderByStockAndCreatedAtDesc(categoryId, pageable);
		} else {
			// 모든 상품 조회
			page = productRepository.findAll(pageable);
		}

		List<ProductListResponse> productListResponses = page.getContent().stream()
			.map(ProductListResponse::of)
			.toList();

		return ProductListPageResponse.builder()
			.products(productListResponses)
			.currentPage(page.getNumber())
			.pageSize(page.getSize())
			.hasNext(page.hasNext())
			.build();
	}

	/**
	 * 상품 단건 조회 메서드
	 * @param productId 상품 고유 ID
	 * @return {@link ProductDetailResponse}
	 */
	@Transactional(readOnly = true)
	public ProductDetailResponse getProductById(Long productId) {
		Product product = findById(productId);

		if (product.getProductType().equals(ProductType.DIGITAL)) {
			List<DigitalContent> digitalContents = digitalContentRepository.findAllByProductId(productId);
			List<DigitalContentDetailResponse> digitalContentDetailResponses = digitalContents.stream()
				.map(DigitalContentDetailResponse::of)
				.toList();
			return ProductDetailResponse.of(product, digitalContentDetailResponses);
		}
		return ProductDetailResponse.of(product);
	}

	/**
	 * 상품 수정 메서드
	 * @param request ProductUpdateRequest
	 * @param productId 상품 고유 ID
	 * @param userDetails 인증된 유저 정보
	 */
	@Transactional
	public void updateProduct(ProductUpdateRequest request, Long productId, CustomUserDetails userDetails) {
		Member member = memberUtilService.findMember(userDetails.getId());
		Product product = findById(productId);
		memberUtilService.checkExpertAuthorization(member, product);
		Product updateProduct = Product.update(request, product);
		productRepository.save(updateProduct);
	}

	/**
	 * 상품 삭제 메서드
	 * @param productId 상품 고유 ID
	 * @param userDetails 인증된 유저 정보
	 */
	@Transactional
	public void deleteProduct(Long productId, CustomUserDetails userDetails) {
		Member member = memberUtilService.findMember(userDetails.getId());
		Product product = findById(productId);
		memberUtilService.checkExpertAuthorization(member, product);
		productRepository.delete(product);
	}

	/**
	 * 상품ID로 상품 검색 메서드
	 * @param id 상품 고유 ID
	 * @return {@link Product}
	 */
	@Transactional(readOnly = true)
	public Product findById(Long id) {
		return productRepository.findById(id)
			.orElseThrow(() -> new ServiceException(ErrorCode.PRODUCT_NOT_FOUND));
	}

	public void validateDigitalProduct(ProductCreateRequest request) {
		if (ProductType.DIGITAL.equals(request.getProductType()) && (Objects.isEmpty(request.getDigitalContents()))) {
			throw new ServiceException(ErrorCode.INVALID_PRODUCT_CONTENT);
		}
	}

	/**
	 * 상품 재고 감소 메서드
	 * @param product 상품
	 * @param quantity 감소시킬 수량
	 * @return 업데이트된 상품
	 */
	public synchronized Product decreaseStock(Product product, Integer quantity) {
		if (product.getStock() < quantity) {
			throw new ServiceException(ErrorCode.INSUFFICIENT_STOCK);
		}

		Product updateProduct = Product.builder()
			.id(product.getId())
			.expert(product.getExpert())
			.category(product.getCategory())
			.title(product.getTitle())
			.description(product.getDescription())
			.thumbnailImage(product.getThumbnailImage())
			.price(product.getPrice())
			.stock(product.getStock() - quantity)
			.productType(product.getProductType())
			.status(product.getStatus())
			.build();

		return productRepository.save(updateProduct);
	}

	@Transactional
	public ProductListPageResponse findMySellingProducts(Pageable pageable, CustomUserDetails userDetails) {
		Member member = memberUtilService.findMember(userDetails.getId());
		Expert expert = memberUtilService.validateExpert(member);

		// Page<Product> page = productRepository.findMySellingProducts(expert.getId(), pageable);
		Page<Product> page = productRepository.findAllByExpertId(expert.getId(), pageable);

		List<ProductListResponse> productListResponses = page.getContent().stream()
			.map(ProductListResponse::of)
			.toList();

		return ProductListPageResponse.builder()
			.products(productListResponses)
			.currentPage(page.getNumber())
			.pageSize(page.getSize())
			.hasNext(page.hasNext())
			.build();
	}
}

package com.ll.dopdang.domain.store.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ll.dopdang.domain.expert.category.entity.Category;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.store.dto.ProductCreateRequest;
import com.ll.dopdang.domain.store.dto.ProductUpdateRequest;
import com.ll.dopdang.global.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product", indexes = {
	@Index(name = "fk_product_expert", columnList = "expert_id")
})
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@JoinColumn(name = "expert_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Expert expert;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private Category category;

	@Size(max = 200)
	@NotNull
	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "description")
	private String description;

	@Size(max = 255)
	@Column(name = "thumbnail_image")
	private String thumbnailImage;

	@NotNull
	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@NotNull
	@Column(name = "stock", nullable = false)
	private Integer stock;

	@NotNull
	@Column(name = "product_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductType productType;

	@NotNull
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	@Builder.Default
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DigitalContent> digitalContentList = new ArrayList<>();

	/**
	 * 상품 생성 메서드
	 * @param request ProductCreateRequest
	 * @param category Cateogory
	 * @param expert Expert
	 */
	public static Product from(ProductCreateRequest request, Category category, Expert expert) {
		Product product = Product.builder()
			.expert(expert)
			.category(category)
			.title(request.title())
			.description(request.description())
			.thumbnailImage(request.thumbnailImage())
			.price(request.price())
			.stock(request.stock())
			.productType(request.productType())
			.status(ProductStatus.AVAILABLE) // Default status
			.build();
		if (Objects.equals(request.productType(), ProductType.DIGITAL) && request.digitalContents() != null) {
			request.digitalContents().forEach(req -> {
				DigitalContent digitalContent = DigitalContent.from(req, product);
				product.getDigitalContentList().add(digitalContent);
			});
		}
		return product;
	}

	/**
	 * 상품 수정 메서드
	 * @param request ProductUpdateRequest
	 * @param product 수정할 상품
	 * @return {@link Product}
	 */
	public static Product update(ProductUpdateRequest request, Product product) {
		Product updateProduct = Product.builder()
			.id(product.getId())
			.expert(product.getExpert())
			.category(product.getCategory())
			.title(request.title())
			.description(request.description())
			.thumbnailImage(request.thumbnailImage())
			.price(request.price())
			.stock(request.stock())
			.productType(product.getProductType())
			.status(product.getStatus()) // Default status
			.build();
		if (Objects.equals(product.getProductType(), ProductType.DIGITAL) && request.digitalContents() != null) {
			request.digitalContents().forEach(req -> {
				DigitalContent digitalContent = DigitalContent.update(req, updateProduct);
				updateProduct.getDigitalContentList().add(digitalContent);
			});
		}
		return updateProduct;
	}
}

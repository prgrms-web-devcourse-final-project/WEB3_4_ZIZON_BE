package com.ll.dopdang.domain.store.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.expert.category.entity.Category;
import com.ll.dopdang.domain.expert.category.entity.CategoryType;
import com.ll.dopdang.domain.expert.category.repository.CategoryRepository;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.member.dto.request.LoginRequest;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.store.dto.ProductCreateRequest;
import com.ll.dopdang.domain.store.dto.ProductUpdateRequest;
import com.ll.dopdang.domain.store.entity.Product;
import com.ll.dopdang.domain.store.entity.ProductStatus;
import com.ll.dopdang.domain.store.entity.ProductType;
import com.ll.dopdang.domain.store.repository.ProductRepository;
import com.ll.dopdang.global.config.S3Config;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.sms.service.CoolSmsService;
import com.ll.dopdang.standard.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {
	@Autowired
	private MockMvc mvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ExpertRepository expertRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@MockBean
	private RedisRepository redisRepository;

	@MockBean
	private CoolSmsService coolSmsService;

	@MockBean
	private S3Config s3Config;

	@MockBean
	private S3Presigner s3Presigner;

	private Member testMember;
	private Category testCategory;
	private Expert testExpert;
	private Product testProduct;

	@BeforeEach
	void setUp() {
		// 기존 데이터 정리
		productRepository.deleteAll();
		expertRepository.deleteAll();
		categoryRepository.deleteAll();
		memberRepository.deleteAll();

		// 테스트 멤버 생성
		testMember = Member.builder()
			.email("test@test.com")
			.password(passwordEncoder.encode("test1234!"))
			.name("테스트유저")
			.profileImage("")
			.memberId("test@test.com")
			.phone("")
			.userRole(MemberRole.EXPERT.toString())
			.status(MemberStatus.ACTIVE.toString())
			.build();
		memberRepository.save(testMember);

		// 테스트 카테고리 생성
		testCategory = Category.builder()
			.id(1L)
			.name("테스트카테고리")
			.categoryType(CategoryType.PRODUCT)
			.build();
		categoryRepository.save(testCategory);

		// 테스트 전문가 생성 (양방향 관계 설정)
		testExpert = Expert.createExpert(
			testMember,
			testCategory,
			"테스트 소개",
			5,
			true,
			"테스트은행",
			"1234567890",
			true
		);
		expertRepository.save(testExpert);

		// 테스트 제품 생성
		testProduct = Product.builder()
			.expert(testExpert)
			.category(testCategory)
			.title("테스트제품")
			.description("테스트제품 설명")
			.price(new BigDecimal("10000"))
			.stock(10)
			.productType(ProductType.PHYSICAL)
			.status(ProductStatus.AVAILABLE)
			.build();
		productRepository.save(testProduct);
	}

	private Cookie performLoginAndGetAccessToken() throws Exception {
		MvcResult result = mvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("test@test.com", "test1234!"))))
			.andExpect(status().isOk())
			.andReturn();

		Cookie[] cookies = result.getResponse().getCookies();
		return Arrays.stream(cookies)
			.filter(cookie -> "accessToken".equals(cookie.getName()))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
	}

	@Test
	@DisplayName("제품 생성 테스트 - 인증된 사용자")
	void testCreateProduct() throws Exception {
		// 로그인하여 액세스 토큰 획득
		Cookie accessTokenCookie = performLoginAndGetAccessToken();

		// 제품 생성 요청 데이터
		ProductCreateRequest request = ProductCreateRequest.builder()
			.categoryId(testCategory.getId())
			.title("새 제품")
			.description("새 제품 설명")
			.price(new BigDecimal("20000"))
			.stock(20)
			.productType(ProductType.PHYSICAL)
			.build();

		// 제품 생성 API 호출
		mvc.perform(post("/products")
				.cookie(accessTokenCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("제품이 성공적으로 등록되었습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("제품 생성 테스트 - 인증되지 않은 사용자")
	void testCreateProductUnauthenticated() throws Exception {
		// 제품 생성 요청 데이터
		ProductCreateRequest request = ProductCreateRequest.builder()
			.categoryId(testCategory.getId())
			.title("새 제품")
			.description("새 제품 설명")
			.price(new BigDecimal("20000"))
			.stock(20)
			.productType(ProductType.PHYSICAL)
			.build();

		// 인증 없이 제품 생성 API 호출
		mvc.perform(post("/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andDo(print());
	}

	@Test
	@DisplayName("제품 목록 조회 테스트 - 인증된 사용자")
	void testGetAllProducts() throws Exception {
		// 로그인하여 액세스 토큰 획득
		Cookie accessTokenCookie = performLoginAndGetAccessToken();

		// 제품 목록 조회 API 호출
		mvc.perform(get("/products")
				.cookie(accessTokenCookie)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.products").isArray())
			.andDo(print());
	}

	@Test
	@DisplayName("제품 목록 조회 테스트 - 인증되지 않은 사용자")
	void testGetAllProductsUnauthenticated() throws Exception {
		// 인증 없이 제품 목록 조회 API 호출
		mvc.perform(get("/products")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("제품 상세 조회 테스트 - 인증된 사용자")
	void testGetProductById() throws Exception {
		// 로그인하여 액세스 토큰 획득
		Cookie accessTokenCookie = performLoginAndGetAccessToken();

		// 제품 상세 조회 API 호출
		mvc.perform(get("/products/{product_id}", testProduct.getId())
				.cookie(accessTokenCookie)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("테스트제품"))
			.andExpect(jsonPath("$.price").value(10000))
			.andDo(print());
	}

	@Test
	@DisplayName("제품 상세 조회 테스트 - 인증되지 않은 사용자")
	void testGetProductByIdUnauthenticated() throws Exception {
		// 인증 없이 제품 상세 조회 API 호출
		mvc.perform(get("/products/{product_id}", testProduct.getId())
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}

	@Test
	@DisplayName("제품 수정 테스트 - 인증된 사용자")
	void testUpdateProduct() throws Exception {
		// 로그인하여 액세스 토큰 획득
		Cookie accessTokenCookie = performLoginAndGetAccessToken();

		// 제품 수정 요청 데이터
		ProductUpdateRequest request = ProductUpdateRequest.builder()
			.title("수정된 제품")
			.description("수정된 제품 설명")
			.price(new BigDecimal("30000"))
			.stock(30)
			.build();

		// 제품 수정 API 호출
		mvc.perform(patch("/products/{product_id}", testProduct.getId())
				.cookie(accessTokenCookie)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("제품이 성공적으로 수정되었습니다."))
			.andDo(print());
	}

	@Test
	@DisplayName("제품 삭제 테스트 - 인증된 사용자")
	void testDeleteProduct() throws Exception {
		// 로그인하여 액세스 토큰 획득
		Cookie accessTokenCookie = performLoginAndGetAccessToken();

		// 제품 삭제 API 호출
		mvc.perform(delete("/products/{product_id}", testProduct.getId())
				.cookie(accessTokenCookie)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("제품이 성공적으로 삭제되었습니다."))
			.andDo(print());
	}
}

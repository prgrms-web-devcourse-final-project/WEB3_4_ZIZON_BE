// package com.ll.dopdang.domain.payment.controller;

// import static org.hamcrest.Matchers.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.Map;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import org.springframework.transaction.annotation.Transactional;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.ll.dopdang.domain.expert.category.entity.Category;
// import com.ll.dopdang.domain.expert.category.entity.CategoryType;
// import com.ll.dopdang.domain.expert.category.repository.CategoryRepository;
// import com.ll.dopdang.domain.expert.entity.Expert;
// import com.ll.dopdang.domain.expert.repository.ExpertRepository;
// import com.ll.dopdang.domain.member.dto.request.LoginRequest;
// import com.ll.dopdang.domain.member.entity.Member;
// import com.ll.dopdang.domain.member.repository.MemberRepository;
// import com.ll.dopdang.domain.payment.client.TossPaymentClient;
// import com.ll.dopdang.domain.payment.dto.OrderIdRequest;
// import com.ll.dopdang.domain.payment.dto.PaymentCancellationRequest;
// import com.ll.dopdang.domain.payment.entity.Payment;
// import com.ll.dopdang.domain.payment.entity.PaymentStatus;
// import com.ll.dopdang.domain.payment.entity.PaymentType;
// import com.ll.dopdang.domain.payment.repository.PaymentRepository;
// import com.ll.dopdang.domain.project.entity.Contract;
// import com.ll.dopdang.domain.project.entity.Project;
// import com.ll.dopdang.domain.project.entity.ProjectStatus;
// import com.ll.dopdang.domain.project.repository.ContractRepository;
// import com.ll.dopdang.domain.project.repository.ProjectRepository;
// import com.ll.dopdang.domain.store.entity.Product;
// import com.ll.dopdang.domain.store.entity.ProductStatus;
// import com.ll.dopdang.domain.store.entity.ProductType;
// import com.ll.dopdang.domain.store.repository.ProductRepository;
// import com.ll.dopdang.global.s3.S3Service;

// import jakarta.servlet.http.Cookie;
// import lombok.extern.slf4j.Slf4j;

// /** 실제 서비스 구현체를 사용하고 외부 API 호출만 모킹합니다.
//  *
//  * 다음 테스트 케이스들이 구현되어 있습니다:
//  * - PROJECT 타입 - 주문 생성 -> 결제 완료 테스트
//  * - ORDER 타입 - 주문 생성 -> 결제 완료 테스트
//  * - 주문 생성 -> 결제 금액 검증 실패 테스트
//  * - 주문 생성 -> 결제 실패 테스트
//  * - 부분 취소 테스트
//  * - 전체 취소 테스트
//  */
// @Slf4j
// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// @Transactional
// public class PaymentControllerIntegrationTest {

// 	@Autowired
// 	private MockMvc mockMvc;

// 	@Autowired
// 	private ObjectMapper objectMapper;

// 	@Autowired
// 	private MemberRepository memberRepository;

// 	@Autowired
// 	private CategoryRepository categoryRepository;

// 	@Autowired
// 	private ExpertRepository expertRepository;

// 	@Autowired
// 	private ProductRepository productRepository;

// 	@Autowired
// 	private ProjectRepository projectRepository;

// 	@Autowired
// 	private ContractRepository contractRepository;

// 	@Autowired
// 	private PaymentRepository paymentRepository;

// 	@Autowired
// 	private PasswordEncoder passwordEncoder;

// 	// 외부 API 호출만 모킹
// 	@MockBean
// 	private TossPaymentClient tossPaymentClient;

// 	@MockBean
// 	private S3Service s3Service;

// 	private Member testMember;
// 	private Category testCategory;
// 	private Expert testExpert;
// 	private Product testProduct;
// 	private Project testProject;
// 	private Contract testContract;
// 	private String testOrderId;

// 	@BeforeEach
// 	void setUp() {
// 		// 테스트 멤버 생성
// 		testMember = Member.builder()
// 			.email("test@test.com")
// 			.password(passwordEncoder.encode("test1234!"))
// 			.name("테스트유저")
// 			.phone("01012345678")
// 			.memberId("test@test.com")
// 			.userRole("CLIENT")
// 			.status("ACTIVE")
// 			.uniqueKey("member_unique_key")
// 			.build();
// 		memberRepository.save(testMember);

// 		// 테스트 카테고리 생성
// 		testCategory = Category.builder()
// 			.name("테스트 카테고리")
// 			.categoryType(CategoryType.PRODUCT)
// 			.build();
// 		categoryRepository.save(testCategory);

// 		// 테스트 전문가 생성
// 		testExpert = Expert.createExpert(
// 			testMember,
// 			testCategory,
// 			"테스트 전문가 소개",
// 			5,
// 			true,
// 			"테스트 은행",
// 			"1111-2222-3333-4444",
// 			true
// 		);
// 		expertRepository.save(testExpert);

// 		// 테스트 상품 생성 (ORDER 타입 테스트용)
// 		testProduct = Product.builder()
// 			.expert(testExpert)
// 			.category(testCategory)
// 			.title("테스트 상품")
// 			.description("테스트 상품 설명")
// 			.price(BigDecimal.valueOf(50000))
// 			.stock(10)
// 			.productType(ProductType.DIGITAL)
// 			.status(ProductStatus.AVAILABLE)
// 			.build();
// 		productRepository.save(testProduct);

// 		// 테스트 프로젝트 생성 (PROJECT 타입 테스트용)
// 		testProject = Project.builder()
// 			.client(testMember)
// 			.category(testCategory)
// 			.title("테스트 프로젝트")
// 			.description("테스트 프로젝트 설명")
// 			.budget(new BigDecimal(10000))
// 			.deadline(LocalDateTime.now().plusDays(30))
// 			.status(ProjectStatus.OPEN)
// 			.expert(testExpert)
// 			.summary("테스트 프로젝트 개요")
// 			.region("서울")
// 			.build();
// 		projectRepository.save(testProject);

// 		// 테스트 계약 생성 (PROJECT 타입 테스트용)
// 		testContract = Contract.builder()
// 			.project(testProject)
// 			.expert(testExpert)
// 			.client(testMember)
// 			.price(BigDecimal.valueOf(100000))
// 			.build();
// 		contractRepository.save(testContract);

// 		// 테스트 주문 ID 설정
// 		testOrderId = "test_order_id";

// 		// TossPaymentClient 모킹 설정
// 		// 결제 승인 성공 응답
// 		String successResponse = "{"
// 			+ "\"mId\":\"tosspayments\","
// 			+ "\"version\":\"1.3\","
// 			+ "\"paymentKey\":\"test_payment_key\","
// 			+ "\"orderId\":\"" + testOrderId + "\","
// 			+ "\"orderName\":\"테스트 결제\","
// 			+ "\"currency\":\"KRW\","
// 			+ "\"method\":\"카드\","
// 			+ "\"totalAmount\":100000,"
// 			+ "\"balanceAmount\":100000,"
// 			+ "\"status\":\"DONE\","
// 			+ "\"requestedAt\":\"2023-01-01T00:00:00+09:00\","
// 			+ "\"approvedAt\":\"2023-01-01T00:00:01+09:00\","
// 			+ "\"useEscrow\":false,"
// 			+ "\"cultureExpense\":false"
// 			+ "}";

// 		// 결제 취소 성공 응답
// 		String cancelResponse = "{"
// 			+ "\"mId\":\"tosspayments\","
// 			+ "\"version\":\"1.3\","
// 			+ "\"paymentKey\":\"test_payment_key\","
// 			+ "\"orderId\":\"" + testOrderId + "\","
// 			+ "\"orderName\":\"테스트 결제\","
// 			+ "\"currency\":\"KRW\","
// 			+ "\"method\":\"카드\","
// 			+ "\"totalAmount\":100000,"
// 			+ "\"balanceAmount\":0,"
// 			+ "\"status\":\"CANCELED\","
// 			+ "\"requestedAt\":\"2023-01-01T00:00:00+09:00\","
// 			+ "\"approvedAt\":\"2023-01-01T00:00:01+09:00\","
// 			+ "\"canceledAt\":\"2023-01-01T00:00:02+09:00\","
// 			+ "\"useEscrow\":false,"
// 			+ "\"cultureExpense\":false,"
// 			+ "\"cancels\":[{"
// 			+ "\"cancelAmount\":100000,"
// 			+ "\"cancelReason\":\"테스트 취소\","
// 			+ "\"canceledAt\":\"2023-01-01T00:00:02+09:00\","
// 			+ "\"taxFreeAmount\":0"
// 			+ "}]"
// 			+ "}";

// 		// 부분 취소 성공 응답
// 		String partialCancelResponse = "{"
// 			+ "\"mId\":\"tosspayments\","
// 			+ "\"version\":\"1.3\","
// 			+ "\"paymentKey\":\"test_payment_key\","
// 			+ "\"orderId\":\"" + testOrderId + "\","
// 			+ "\"orderName\":\"테스트 결제\","
// 			+ "\"currency\":\"KRW\","
// 			+ "\"method\":\"카드\","
// 			+ "\"totalAmount\":100000,"
// 			+ "\"balanceAmount\":50000,"
// 			+ "\"status\":\"PARTIAL_CANCELED\","
// 			+ "\"requestedAt\":\"2023-01-01T00:00:00+09:00\","
// 			+ "\"approvedAt\":\"2023-01-01T00:00:01+09:00\","
// 			+ "\"canceledAt\":\"2023-01-01T00:00:02+09:00\","
// 			+ "\"useEscrow\":false,"
// 			+ "\"cultureExpense\":false,"
// 			+ "\"cancels\":[{"
// 			+ "\"cancelAmount\":50000,"
// 			+ "\"cancelReason\":\"테스트 부분 취소\","
// 			+ "\"canceledAt\":\"2023-01-01T00:00:02+09:00\","
// 			+ "\"taxFreeAmount\":0"
// 			+ "}]"
// 			+ "}";

// 		// 결제 승인 API 모킹
// 		when(tossPaymentClient.confirmPayment(anyString(), anyMap()))
// 			.thenReturn(new ResponseEntity<>(successResponse, HttpStatus.OK));

// 		// 결제 취소 API 모킹 - 전액 취소
// 		when(tossPaymentClient.cancelPayment(anyString(), eq("test_payment_key"), argThat(map ->
// 			map.containsKey("cancelReason") && !map.containsKey("cancelAmount"))))
// 			.thenReturn(new ResponseEntity<>(cancelResponse, HttpStatus.OK));

// 		// 결제 취소 API 모킹 - 부분 취소
// 		when(tossPaymentClient.cancelPayment(anyString(), eq("test_payment_key"), argThat(map ->
// 			map.containsKey("cancelReason") && map.containsKey("cancelAmount"))))
// 			.thenReturn(new ResponseEntity<>(partialCancelResponse, HttpStatus.OK));
// 	}

// 	private Cookie performLoginAndGetAccessToken() throws Exception {
// 		String loginJson = objectMapper.writeValueAsString(new LoginRequest("test@test.com", "test1234!"));

// 		var result = mockMvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(loginJson))
// 			.andExpect(status().isOk())
// 			.andReturn();

// 		Cookie[] cookies = result.getResponse().getCookies();
// 		return Arrays.stream(cookies)
// 			.filter(cookie -> "accessToken".equals(cookie.getName()))
// 			.findFirst()
// 			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
// 	}

// 	@Test
// 	@DisplayName("PROJECT 타입 - 주문 생성 -> 결제 완료 테스트")
// 	void testProjectPaymentSuccess() throws Exception {
// 		// given
// 		OrderIdRequest request = new OrderIdRequest(PaymentType.PROJECT, testContract.getId(), 1);
// 		String requestJson = objectMapper.writeValueAsString(request);
// 		Cookie accessToken = performLoginAndGetAccessToken();

// 		// 1. 주문 ID 생성
// 		MvcResult orderIdResult = mockMvc.perform(post("/payments/orderId")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderId").exists())
// 			.andReturn();

// 		// 주문 ID 추출
// 		Map<String, Object> responseMap = objectMapper.readValue(
// 			orderIdResult.getResponse().getContentAsString(), Map.class);
// 		String orderId = (String)responseMap.get("orderId");

// 		// 2. 결제 완료 처리
// 		mockMvc.perform(get("/payments/success")
// 				.param("paymentKey", "test_payment_key")
// 				.param("orderId", orderId)
// 				.param("amount", "100000")
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("success"))
// 			.andExpect(jsonPath("$.amount").value(100000))
// 			.andExpect(jsonPath("$.paymentName").value("테스트 프로젝트"));

// 		// 3. 결제 정보 확인
// 		Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
// 		assert payment != null;
// 		assert payment.getPaymentType() == PaymentType.PROJECT;
// 		assert payment.getStatus() == PaymentStatus.PAID;
// 		assert payment.getTotalPrice().compareTo(BigDecimal.valueOf(100000)) == 0;
// 	}

// 	@Test
// 	@DisplayName("ORDER 타입 - 주문 생성 -> 결제 완료 테스트")
// 	void testOrderPaymentSuccess() throws Exception {
// 		// given
// 		OrderIdRequest request = new OrderIdRequest(PaymentType.ORDER, testProduct.getId(), 1);
// 		String requestJson = objectMapper.writeValueAsString(request);
// 		Cookie accessToken = performLoginAndGetAccessToken();

// 		// 1. 주문 ID 생성
// 		MvcResult orderIdResult = mockMvc.perform(post("/payments/orderId")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderId").exists())
// 			.andReturn();

// 		// 주문 ID 추출
// 		Map<String, Object> responseMap = objectMapper.readValue(
// 			orderIdResult.getResponse().getContentAsString(), Map.class);
// 		String orderId = (String)responseMap.get("orderId");

// 		// 2. 결제 완료 처리
// 		mockMvc.perform(get("/payments/success")
// 				.param("paymentKey", "test_payment_key")
// 				.param("orderId", orderId)
// 				.param("amount", "50000")
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("success"))
// 			.andExpect(jsonPath("$.amount").value(50000))
// 			.andExpect(jsonPath("$.paymentName").value("테스트 상품"));

// 		// 3. 결제 정보 확인
// 		Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
// 		assert payment != null;
// 		assert payment.getPaymentType() == PaymentType.ORDER;
// 		assert payment.getStatus() == PaymentStatus.PAID;
// 		assert payment.getTotalPrice().compareTo(BigDecimal.valueOf(50000)) == 0;
// 	}

// 	@Test
// 	@DisplayName("주문 생성 -> 결제 금액 검증 실패 테스트")
// 	void testPaymentAmountValidationFailure() throws Exception {
// 		// given
// 		OrderIdRequest request = new OrderIdRequest(PaymentType.PROJECT, testContract.getId(), 1);
// 		String requestJson = objectMapper.writeValueAsString(request);
// 		Cookie accessToken = performLoginAndGetAccessToken();

// 		// 1. 주문 ID 생성
// 		MvcResult orderIdResult = mockMvc.perform(post("/payments/orderId")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderId").exists())
// 			.andReturn();

// 		// 주문 ID 추출
// 		Map<String, Object> responseMap = objectMapper.readValue(
// 			orderIdResult.getResponse().getContentAsString(), Map.class);
// 		String orderId = (String)responseMap.get("orderId");

// 		// 2. 잘못된 금액으로 결제 시도 (계약금액 100,000원 대신 999,999원으로 시도)
// 		mockMvc.perform(get("/payments/success")
// 				.param("paymentKey", "test_payment_key")
// 				.param("orderId", orderId)
// 				.param("amount", "999999")
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isBadRequest())
// 			.andExpect(jsonPath("$.message").value(containsString("금액이 일치하지 않습니다")));

// 		// 3. 결제 정보 확인 - 결제 실패로 인해 저장되지 않아야 함
// 		Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
// 		assert payment == null || payment.getStatus() != PaymentStatus.PAID;
// 	}

// 	@Test
// 	@DisplayName("주문 생성 -> 결제 실패 테스트")
// 	void testPaymentFailure() throws Exception {
// 		// given
// 		OrderIdRequest request = new OrderIdRequest(PaymentType.ORDER, testProduct.getId(), 1);
// 		String requestJson = objectMapper.writeValueAsString(request);
// 		Cookie accessToken = performLoginAndGetAccessToken();

// 		// 1. 주문 ID 생성
// 		MvcResult orderIdResult = mockMvc.perform(post("/payments/orderId")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderId").exists())
// 			.andReturn();

// 		// 주문 ID 추출
// 		Map<String, Object> responseMap = objectMapper.readValue(
// 			orderIdResult.getResponse().getContentAsString(), Map.class);
// 		String orderId = (String)responseMap.get("orderId");

// 		// 2. 결제 실패 처리
// 		mockMvc.perform(get("/payments/fail")
// 				.param("code", "NOT_FOUND_PAYMENT_SESSION")
// 				.param("message", "결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다.")
// 				.param("orderId", orderId)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("fail"))
// 			.andExpect(jsonPath("$.errorCode").value("NOT_FOUND_PAYMENT_SESSION"))
// 			.andExpect(jsonPath("$.message").value("결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다."));

// 		// 3. 결제 정보 확인
// 		Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
// 		assert payment != null;
// 		assert payment.getStatus() == PaymentStatus.FAILED;
// 	}

// 	@Test
// 	@DisplayName("부분 취소 테스트")
// 	void testPartialCancellation() throws Exception {
// 		// given
// 		OrderIdRequest request = new OrderIdRequest(PaymentType.PROJECT, testContract.getId(), 1);
// 		String requestJson = objectMapper.writeValueAsString(request);
// 		Cookie accessToken = performLoginAndGetAccessToken();

// 		// 1. 주문 ID 생성
// 		MvcResult orderIdResult = mockMvc.perform(post("/payments/orderId")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderId").exists())
// 			.andReturn();

// 		// 주문 ID 추출
// 		Map<String, Object> responseMap = objectMapper.readValue(
// 			orderIdResult.getResponse().getContentAsString(), Map.class);
// 		String orderId = (String)responseMap.get("orderId");

// 		// 2. 결제 완료 처리
// 		mockMvc.perform(get("/payments/success")
// 				.param("paymentKey", "test_payment_key")
// 				.param("orderId", orderId)
// 				.param("amount", "100000")
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("success"));

// 		// 3. 부분 취소 요청
// 		PaymentCancellationRequest cancelRequest = new PaymentCancellationRequest(
// 			orderId, "부분 환불 테스트", BigDecimal.valueOf(50000));
// 		String cancelRequestJson = objectMapper.writeValueAsString(cancelRequest);

// 		mockMvc.perform(post("/payments/cancel")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(cancelRequestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("PARTIALLY_CANCELED"))
// 			.andExpect(jsonPath("$.canceledAmount").value(50000))
// 			.andExpect(jsonPath("$.remainingAmount").value(50000));

// 		// 4. 결제 정보 확인
// 		Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
// 		assert payment != null;
// 		assert payment.getStatus() == PaymentStatus.PARTIALLY_CANCELED;
// 		assert payment.getRemainingAmount().compareTo(BigDecimal.valueOf(50000)) == 0;
// 	}

// 	@Test
// 	@DisplayName("전체 취소 테스트")
// 	void testFullCancellation() throws Exception {
// 		// given
// 		OrderIdRequest request = new OrderIdRequest(PaymentType.ORDER, testProduct.getId(), 1);
// 		String requestJson = objectMapper.writeValueAsString(request);
// 		Cookie accessToken = performLoginAndGetAccessToken();

// 		// 1. 주문 ID 생성
// 		MvcResult orderIdResult = mockMvc.perform(post("/payments/orderId")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(requestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.orderId").exists())
// 			.andReturn();

// 		// 주문 ID 추출
// 		Map<String, Object> responseMap = objectMapper.readValue(
// 			orderIdResult.getResponse().getContentAsString(), Map.class);
// 		String orderId = (String)responseMap.get("orderId");

// 		// 2. 결제 완료 처리
// 		mockMvc.perform(get("/payments/success")
// 				.param("paymentKey", "test_payment_key")
// 				.param("orderId", orderId)
// 				.param("amount", "50000")
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("success"));

// 		// 3. 전체 취소 요청 (cancelAmount를 null로 설정)
// 		PaymentCancellationRequest cancelRequest = new PaymentCancellationRequest(
// 			orderId, "전체 환불 테스트", null);
// 		String cancelRequestJson = objectMapper.writeValueAsString(cancelRequest);

// 		mockMvc.perform(post("/payments/cancel")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(cancelRequestJson)
// 				.cookie(accessToken))
// 			.andDo(print())
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.status").value("FULLY_CANCELED"))
// 			.andExpect(jsonPath("$.canceledAmount").value(50000))
// 			.andExpect(jsonPath("$.remainingAmount").value(0));

// 		// 4. 결제 정보 확인
// 		Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
// 		assert payment != null;
// 		assert payment.getStatus() == PaymentStatus.FULLY_CANCELED;
// 		assert payment.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0;
// 	}
// }

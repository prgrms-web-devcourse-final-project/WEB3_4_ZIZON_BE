// package com.ll.dopdang.domain.member.controller;
//
// import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
// import static org.hibernate.validator.internal.util.Contracts.assertTrue;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.util.AssertionErrors.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
// import java.util.Arrays;
// import java.util.concurrent.TimeUnit;
//
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.MvcResult;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.ll.dopdang.domain.member.dto.request.LoginRequest;
// import com.ll.dopdang.domain.member.dto.request.UpdateProfileRequest;
// import com.ll.dopdang.domain.member.entity.Member;
// import com.ll.dopdang.domain.member.entity.MemberRole;
// import com.ll.dopdang.domain.member.entity.MemberStatus;
// import com.ll.dopdang.domain.member.repository.MemberRepository;
// import com.ll.dopdang.global.exception.ErrorCode;
// import com.ll.dopdang.global.exception.ServiceException;
// import com.ll.dopdang.global.redis.repository.RedisRepository;
// import com.ll.dopdang.standard.util.JwtUtil;
//
// import jakarta.servlet.http.Cookie;
//
// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// @Transactional
// class MemberControllerTest {
// 	@Autowired
// 	private MockMvc mvc;
// 	@Autowired
// 	private MemberRepository memberRepository;
// 	@Autowired
// 	private ObjectMapper objectMapper;
// 	@Autowired
// 	private PasswordEncoder passwordEncoder;
// 	@Autowired
// 	private JwtUtil jwtUtil;
// 	@MockBean
// 	private RedisRepository redisRepository;
//
// 	@BeforeEach
// 	void setUp() {
// 		memberRepository.deleteAll();
// 		Member member1 = Member.builder()
// 			.email("test1@test.com")
// 			.password(passwordEncoder.encode("test1234!"))
// 			.name("test1")
// 			.profileImage("")
// 			.memberId("test1@test.com")
// 			.phone("")
// 			.userRole(MemberRole.CLIENT.toString())
// 			.status(MemberStatus.ACTIVE.toString())
// 			.build();
// 		Member member2 = Member.builder()
// 			.email("test2@test.com")
// 			.password(passwordEncoder.encode("test1234!"))
// 			.name("test2")
// 			.profileImage("")
// 			.memberId("test2@test.com")
// 			.phone("")
// 			.userRole(MemberRole.CLIENT.toString())
// 			.status(MemberStatus.ACTIVE.toString())
// 			.build();
// 		Member member3 = Member.builder()
// 			.email("test3@test.com")
// 			.password(passwordEncoder.encode("test1234!"))
// 			.name("test3")
// 			.profileImage("")
// 			.memberId("test3@test.com")
// 			.phone("")
// 			.userRole(MemberRole.CLIENT.toString())
// 			.status(MemberStatus.DEACTIVATED.toString())
// 			.build();
// 		memberRepository.save(member1);
// 		memberRepository.save(member2);
// 		memberRepository.save(member3);
// 	}
//
// 	@Test
// 	@DisplayName("로그인 테스트 - 정상 로그인 시도")
// 	void test1() throws Exception {
// 		MvcResult result = mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.email").value("test1@test.com"))
// 			.andExpect(jsonPath("$.profileImage").value(""))
// 			.andExpect(jsonPath("$.name").value("test1"))
// 			.andDo(print())
// 			.andReturn();
//
// 		Cookie[] cookies = result.getResponse().getCookies();
// 		assertNotNull(cookies, "쿠키가 null입니다.");
// 		assertTrue(cookies.length > 0, "쿠키가 없습니다.");
//
// 		Cookie accessTokenCookie = Arrays.stream(cookies)
// 			.filter(cookie -> "accessToken".equals(cookie.getName()))
// 			.findFirst()
// 			.orElse(null);
//
// 		assertNotNull(accessTokenCookie, "액세스 토큰 쿠키가 없습니다.");
// 		assertFalse("액세스 토큰이 비어있습니다.", accessTokenCookie.getValue().isEmpty());
//
// 		verify(redisRepository, times(1)).save(
// 			eq(accessTokenCookie.getValue()),
// 			anyString(),
// 			anyLong(),
// 			eq(TimeUnit.MILLISECONDS)
// 		);
// 	}
//
// 	@Test
// 	@DisplayName("로그인 테스트 - 이메일 형식 오류")
// 	void test2() throws Exception {
// 		mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1", "test1234!"))))
// 			.andExpect(status().isBadRequest())
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@DisplayName("로그인 테스트 - 비밀번호 형식 오류")
// 	void test3() throws Exception {
// 		mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234"))))
// 			.andExpect(status().isBadRequest())
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@DisplayName("로그인 테스트 - 탈퇴 유저 로그인 시도")
// 	void test4() throws Exception {
// 		mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test3@test.com", "test1234!"))))
// 			.andExpect(status().isForbidden())
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@DisplayName("로그인 테스트 - 이메일, 비밀번호 미입력")
// 	void test5() throws Exception {
// 		mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
// 			.andExpect(status().isBadRequest())
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@DisplayName("로그아웃 테스트")
// 	void test6() throws Exception {
// 		MvcResult loginResult = mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
//
// 		Cookie[] cookies = loginResult.getResponse().getCookies();
// 		assertNotNull(cookies, "로그인 응답에 쿠키가 없습니다.");
// 		assertTrue(cookies.length > 0, "로그인 응답에 쿠키가 없습니다.");
//
// 		Cookie accessTokenCookie = Arrays.stream(cookies)
// 			.filter(cookie -> cookie.getName().equals("accessToken"))
// 			.findFirst()
// 			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
//
// 		String accessTokenValue = accessTokenCookie.getValue();
// 		assertFalse("액세스 토큰이 비어있습니다.", accessTokenValue.isEmpty());
//
// 		Long userId = jwtUtil.getUserId(accessTokenValue);
//
// 		// 3. isClient 값을 false로 설정
// 		Member member = memberRepository.findById(userId)
// 			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));
//
// 		Member updatedMember = Member.builder()
// 			.id(member.getId())
// 			.email(member.getEmail())
// 			.password(member.getPassword())
// 			.name(member.getName())
// 			.profileImage(member.getProfileImage())
// 			.phone(member.getPhone())
// 			.status(member.getStatus())
// 			.userRole(member.getUserRole())
// 			.memberId(member.getMemberId())
// 			.uniqueKey(member.getUniqueKey())
// 			.isClient(false)
// 			.createdAt(member.getCreatedAt())
// 			.updatedAt(member.getUpdatedAt())
// 			.build();
// 		memberRepository.save(updatedMember);
//
// 		// 저장 후 확인
// 		member = memberRepository.findById(userId).orElseThrow();
// 		assertFalse("테스트 전 isClient는 false여야 합니다", member.isClient());
//
// 		MvcResult logoutResult = mvc.perform(post("/users/logout")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.cookie(accessTokenCookie))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.code").value("200"))
// 			.andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
// 			.andDo(print())
// 			.andReturn();
//
// 		Member resultMember = memberRepository.findById(userId).orElseThrow();
// 		assertTrue(resultMember.isClient(), "로그아웃 후 isClient는 true여야 합니다");
//
// 		Cookie[] logoutCookies = logoutResult.getResponse().getCookies();
// 		assertNotNull(logoutCookies, "로그아웃 응답에 쿠키가 없습니다.");
//
// 		boolean accessTokenInvalidated = false;
// 		for (Cookie cookie : logoutCookies) {
// 			if ("accessToken".equals(cookie.getName())) {
// 				assertEquals("쿠키가 무효화되지 않았습니다.", 0, cookie.getMaxAge());
// 				accessTokenInvalidated = true;
// 				break;
// 			}
// 		}
// 		assertTrue(accessTokenInvalidated, "액세스 토큰 쿠키가 무효화되지 않았습니다.");
//
// 		verify(redisRepository, times(1)).remove(eq(accessTokenValue));
//
// 		verify(redisRepository, times(1)).save(
// 			eq("blacklist:" + accessTokenValue),
// 			eq("LOGOUT"),
// 			anyLong(),
// 			eq(TimeUnit.MILLISECONDS)
// 		);
// 	}
//
// 	@Test
// 	@DisplayName("로그아웃 테스트 - 로그인 하지 않은 상태(토큰이 없는 상태)")
// 	void test7() throws Exception {
// 		mvc.perform(post("/users/logout")
// 				.contentType(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isBadRequest())
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@DisplayName("프로필 조회 테스트")
// 	void test8() throws Exception {
// 		MvcResult loginResult = mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
//
// 		Cookie[] cookies = loginResult.getResponse().getCookies();
// 		assertNotNull(cookies, "로그인 응답에 쿠키가 없습니다.");
// 		assertTrue(cookies.length > 0, "로그인 응답에 쿠키가 없습니다.");
//
// 		Cookie accessTokenCookie = Arrays.stream(cookies)
// 			.filter(cookie -> cookie.getName().equals("accessToken"))
// 			.findFirst()
// 			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
//
// 		String accessTokenValue = accessTokenCookie.getValue();
// 		assertFalse("액세스 토큰이 비어있습니다.", accessTokenValue.isEmpty());
//
// 		Long userId = jwtUtil.getUserId(accessTokenValue);
//
// 		mvc.perform(get("/users/{user_id}", userId)
// 				.cookie(accessTokenCookie)
// 				.contentType(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.name").value("test1"))
// 			.andExpect(jsonPath("$.email").value("test1@test.com"))
// 			.andExpect(jsonPath("$.phone").value(""))
// 			.andExpect(jsonPath("$.profileImage").value(""))
// 			.andDo(print())
// 			.andReturn();
// 	}
//
// 	@Test
// 	@DisplayName("프로필 조회 테스트 - 다른 사용자의 접근")
// 	void test9() throws Exception {
// 		Member member2 = memberRepository.findByEmail("test2@test.com")
// 			.orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));
// 		Long otherUserId = member2.getId();
//
// 		MvcResult loginResult = mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
//
// 		Cookie accessTokenCookie = Arrays.stream(loginResult.getResponse().getCookies())
// 			.filter(cookie -> "accessToken".equals(cookie.getName()))
// 			.findFirst()
// 			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
//
// 		mvc.perform(get("/users/{user_id}", otherUserId)
// 				.cookie(accessTokenCookie)
// 				.contentType(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isUnauthorized())
// 			.andExpect(jsonPath("$.message").value("인증되지 않은 유저입니다."))
// 			.andExpect(jsonPath("$.status").value(401))
// 			.andDo(print())
// 			.andReturn();
// 	}
//
// 	@Test
// 	@DisplayName("프로필 수정 테스트")
// 	void test10() throws Exception {
// 		MvcResult loginResult = mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
//
// 		Cookie[] cookies = loginResult.getResponse().getCookies();
// 		assertNotNull(cookies, "로그인 응답에 쿠키가 없습니다.");
// 		assertTrue(cookies.length > 0, "로그인 응답에 쿠키가 없습니다.");
//
// 		Cookie accessTokenCookie = Arrays.stream(cookies)
// 			.filter(cookie -> cookie.getName().equals("accessToken"))
// 			.findFirst()
// 			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
//
// 		String accessTokenValue = accessTokenCookie.getValue();
// 		assertFalse("액세스 토큰이 비어있습니다.", accessTokenValue.isEmpty());
//
// 		String newName = "update1";
// 		String newProfile = "update1234!";
//
// 		Long userId = jwtUtil.getUserId(accessTokenValue);
// 		mvc.perform(patch("/users/{user_id}", userId)
// 				.cookie(accessTokenCookie)
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new UpdateProfileRequest(newName, newProfile))))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
//
// 		Member member = memberRepository.findById(userId)
// 			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));
// 		assertEquals("이름이 변경되지 않음", newName, member.getName());
//
// 		mvc.perform(get("/users/{user_id}", userId)
// 				.cookie(accessTokenCookie)
// 				.contentType(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andExpect(jsonPath("$.name").value(newName))
// 			.andExpect(jsonPath("$.profileImage").value(newProfile))
// 			.andDo(print());
// 	}
//
// 	@Test
// 	@DisplayName("탈퇴 테스트")
// 	void test11() throws Exception {
// 		MvcResult loginResult = mvc.perform(post("/users/login")
// 				.contentType(MediaType.APPLICATION_JSON)
// 				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
//
// 		Cookie[] cookies = loginResult.getResponse().getCookies();
// 		assertNotNull(cookies, "로그인 응답에 쿠키가 없습니다.");
// 		assertTrue(cookies.length > 0, "로그인 응답에 쿠키가 없습니다.");
//
// 		Cookie accessTokenCookie = Arrays.stream(cookies)
// 			.filter(cookie -> cookie.getName().equals("accessToken"))
// 			.findFirst()
// 			.orElseThrow(() -> new RuntimeException("액세스 토큰 쿠키를 찾을 수 없습니다."));
//
// 		String accessTokenValue = accessTokenCookie.getValue();
// 		assertFalse("액세스 토큰이 비어있습니다.", accessTokenValue.isEmpty());
//
// 		Long userId = jwtUtil.getUserId(accessTokenValue);
// 		mvc.perform(delete("/users/{user_id}", userId)
// 				.cookie(accessTokenCookie)
// 				.contentType(MediaType.APPLICATION_JSON))
// 			.andExpect(status().isOk())
// 			.andDo(print())
// 			.andReturn();
// 		Member member = memberRepository.findById(userId).orElseThrow(
// 			() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND)
// 		);
// 		assertEquals("유저 상태", member.getStatus(), "DEACTIVATED");
// 	}
// }

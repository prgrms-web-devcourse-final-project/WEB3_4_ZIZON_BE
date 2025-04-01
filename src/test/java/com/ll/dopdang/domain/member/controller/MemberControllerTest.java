package com.ll.dopdang.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.dto.request.LoginRequest;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.entity.MemberStatus;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.redis.repository.RedisRepository;
import com.ll.dopdang.global.sms.service.CoolSmsService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@MockitoBean
	private RedisRepository redisRepository;
	@MockitoBean
	private CoolSmsService coolSmsService;

	@BeforeEach
	void setUp() {
		memberRepository.deleteAll();
		Member member1 = Member.builder()
			.email("test1@test.com")
			.password(passwordEncoder.encode("test1234!"))
			.name("test1")
			.profileImage("")
			.memberId("test1@test.com")
			.userRole(MemberRole.ROLE_CLIENT.toString())
			.status(MemberStatus.ACTIVE.toString())
			.build();
		Member member2 = Member.builder()
			.email("test2@test.com")
			.password(passwordEncoder.encode("test1234!"))
			.name("test2")
			.profileImage("")
			.memberId("test2@test.com")
			.userRole(MemberRole.ROLE_CLIENT.toString())
			.status(MemberStatus.ACTIVE.toString())
			.build();
		Member member3 = Member.builder()
			.email("test3@test.com")
			.password(passwordEncoder.encode("test1234!"))
			.name("test3")
			.profileImage("")
			.memberId("test3@test.com")
			.userRole(MemberRole.ROLE_CLIENT.toString())
			.status(MemberStatus.DEACTIVATED.toString())
			.build();
		memberRepository.save(member1);
		memberRepository.save(member2);
		memberRepository.save(member3);
	}

	@Test
	@DisplayName("로그인 테스트 - 정상 로그인 시도")
	void test1() throws Exception {
		mvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234!"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.body.email").value("test1@test.com"))
			.andExpect(jsonPath("$.body.profileImage").value(""))
			.andExpect(jsonPath("$.body.name").value("test1"))
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 테스트 - 이메일 형식 오류")
	void test2() throws Exception {
		mvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("test1", "test1234!"))))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 테스트 - 비밀번호 형식 오류")
	void test3() throws Exception {
		mvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("test1@test.com", "test1234"))))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 테스트 - 탈퇴 유저 로그인 시도")
	void test4() throws Exception {
		mvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("test3@test.com", "test1234!"))))
			.andExpect(status().isForbidden())
			.andDo(print());
	}

	@Test
	@DisplayName("로그인 테스트 - 이메일, 비밀번호 미입력")
	void test5() throws Exception {
		mvc.perform(post("/users/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
			.andExpect(status().isBadRequest())
			.andDo(print());
	}
}

package com.ll.dopdang.global.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.security.custom.CustomUserDetailsService;
import com.ll.dopdang.global.security.jwt.filter.JwtAuthenticationFilter;
import com.ll.dopdang.global.security.jwt.filter.JwtAuthorizationFilter;
import com.ll.dopdang.global.security.jwt.handler.JwtLogoutHandler;
import com.ll.dopdang.global.security.jwt.handler.JwtLogoutSuccessHandler;
import com.ll.dopdang.global.security.jwt.service.TokenManagementService;
import com.ll.dopdang.global.security.jwt.service.TokenService;
import com.ll.dopdang.global.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.ll.dopdang.global.security.oauth2.handler.OAuth2LoginSuccessHandler;
import com.ll.dopdang.global.security.oauth2.service.CustomOAuth2UserService;
import com.ll.dopdang.standard.util.AuthResponseUtil;
import com.ll.dopdang.standard.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * SecurityConfig
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtUtil jwtUtil;
	private final ObjectMapper objectMapper;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
	private final TokenService tokenService;
	private final TokenManagementService tokenManagementService;
	private final CustomUserDetailsService userDetailsService;
	private final MemberRepository memberRepository;

	/**
	 * 비밀번호 인코딩
	 * @return {@link PasswordEncoder}
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 *
	 * @param authenticationConfiguration auth
	 * @return {@link AuthenticationManager}
	 * @throws Exception 예외 처리
	 */
	@Bean
	public AuthenticationManager authenticationManager(
		AuthenticationConfiguration authenticationConfiguration
	) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		provider.setHideUserNotFoundExceptions(false);
		return provider;
	}

	private static final Map<HttpMethod, List<String>> PUBLIC_URLS = new HashMap<>();

	//권한이 필요 없는 공개 URL 리스트
	static {
		PUBLIC_URLS.put(HttpMethod.GET, Arrays.asList(
			"/h2-console/**",
			"/login/oauth2/code/kakao",
			"/login/oauth2/code/google",
			"/login/oauth2/code/naver",
			"/oauth2/authorization/kakao",
			"/oauth2/authorization/google",
			"/oauth2/authorization/naver",
			"/swagger-ui/**",
			"/api-docs/**",
			"/projects/all",
			"/api/s3/**",
			"/experts/**",
			"/posts/**"
		));
		PUBLIC_URLS.put(HttpMethod.POST, Arrays.asList(
			"/users/login",
			"/users/signup",
			"/api/s3/**"
		));
	}

	/**
	 *
	 * @param http HttpSecurity
	 * @param configuration AuthenticationConfiguration
	 * @return {@link SecurityFilterChain}
	 * @throws Exception 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration configuration) throws
		Exception {
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
			tokenManagementService, objectMapper, authenticationManager(configuration));
		jwtAuthenticationFilter.setFilterProcessesUrl("/users/login");

		JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(
			jwtUtil, tokenManagementService, objectMapper);

		http.headers(head -> head
				.frameOptions(option -> option.sameOrigin()))
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorizeRequests -> {
				PUBLIC_URLS.forEach((method, urls) ->
					urls.forEach(url -> authorizeRequests.requestMatchers(method, url).permitAll()));

				authorizeRequests.anyRequest().authenticated();
			})
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> AuthResponseUtil.failLogin(
					response, (ResponseEntity<?>)ResponseEntity.badRequest(), HttpServletResponse.SC_UNAUTHORIZED,
					objectMapper
				)))
			.exceptionHandling(exception -> exception
				.accessDeniedHandler((request, response, authException) -> AuthResponseUtil.failLogin(
					response, (ResponseEntity<?>)ResponseEntity.badRequest(), HttpServletResponse.SC_FORBIDDEN,
					objectMapper
				)))
			.logout(logout -> logout
				.logoutUrl("/users/logout")
				.addLogoutHandler(new JwtLogoutHandler(tokenService, tokenManagementService, jwtUtil, memberRepository))
				.logoutSuccessHandler(new JwtLogoutSuccessHandler(objectMapper)))
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService))
				.successHandler(oAuth2LoginSuccessHandler)
				.failureHandler(oAuth2LoginFailureHandler))
			.authenticationProvider(authenticationProvider());
		return http.build();
	}

	/**
	 *
	 * @return {@link UrlBasedCorsConfigurationSource}
	 */
	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 허용할 HTTP 메서드 설정
		configuration.setAllowedMethods(
			Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
		);

		// CORS 설정
		configuration.setAllowedOrigins(
			List.of("http://localhost:8080", "http://localhost:3000")
		);

		// 자격 증명 허용 설정
		configuration.setAllowCredentials(true);

		// 허용할 헤더 설정
		configuration.setAllowedHeaders(List.of("*"));

		configuration.setExposedHeaders(
			Arrays.asList("Authorization", "Set-Cookie", "Access-Control-Allow-Credentials"));

		// CORS 설정을 소스에 등록
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	/**
	 * 공개 URL 리스트 가져오기
	 * @return {@link Map}
	 */
	public static Map<HttpMethod, List<String>> getPublicUrls() {
		return PUBLIC_URLS;
	}
}

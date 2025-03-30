package com.ll.dopdang.global.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(
		AuthenticationConfiguration authenticationConfiguration
	) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	private static final Map<HttpMethod, List<String>> PUBLIC_URLS = new HashMap<>();

	// 허용 URL 리스트
	static {
		PUBLIC_URLS.put(HttpMethod.GET, Arrays.asList(
			"/h2-console/**"
		));
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration configuration) throws
		Exception {
		http.headers(head -> head
				.frameOptions(option -> option.sameOrigin()))
			.csrf(csrf -> csrf.disable())
			.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorizeRequests -> {
				PUBLIC_URLS.forEach((method, urls) ->
					urls.forEach(url -> authorizeRequests.requestMatchers(method, url).permitAll()));

				authorizeRequests.anyRequest().authenticated();
			});

		return http.build();
	}

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

		configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));

		// CORS 설정을 소스에 등록
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	public static Map<HttpMethod, List<String>> getPublicUrls() {
		return PUBLIC_URLS;
	}
}

package com.ll.dopdang.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		Info info = new Info()
			.title("Dopdang API Documentation")
			.version("v1.0.0")
			.description("Dopdang 프로젝트의 API 명세서입니다. 인증이 필요한 API는 먼저 로그인을 수행해야 합니다.")
			.contact(new Contact()
				.name("ZIZON Dev Team")
				.email("makessense7899@gmail.com"));

		// 쿠키 기반 인증을 위한 스키마 설정
		SecurityScheme cookieAuth = new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.COOKIE)
			.name("accessToken");

		return new OpenAPI()
			.info(info)
			.components(new Components()
				.addSecuritySchemes("cookieAuth", cookieAuth));
	}
}

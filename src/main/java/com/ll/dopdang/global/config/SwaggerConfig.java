package com.ll.dopdang.global.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Dopdang API 문서")
				.version("v1.0.0")
				.description("Dopdang 백엔드 API 명세입니다.")
				.contact(new Contact()
					.name("ZIZON Dev Team")
					.email("makessense7899@gmail.com")
				)
			);
	}
}

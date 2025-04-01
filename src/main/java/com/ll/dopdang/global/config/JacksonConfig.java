package com.ll.dopdang.global.config;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ll.dopdang.global.converter.LocalDateTimeConverter;

/**
 * Jackson 설정 클래스
 */
@Configuration
public class JacksonConfig {

	/**
	 * ObjectMapper 설정
	 * LocalDateTime 변환을 위한 커스텀 설정 포함
	 */
	@Bean
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder,
		LocalDateTimeConverter localDateTimeConverter) {
		ObjectMapper objectMapper = builder.createXmlMapper(false).build();

		// JavaTimeModule 등록
		JavaTimeModule javaTimeModule = new JavaTimeModule();

		// LocalDateTime에 커스텀 Deserializer 등록
		javaTimeModule.addDeserializer(LocalDateTime.class, localDateTimeConverter);

		objectMapper.registerModule(javaTimeModule);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		return objectMapper;
	}
}

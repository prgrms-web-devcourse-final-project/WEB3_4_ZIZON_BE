package com.ll.dopdang.global.config;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
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

		// Hibernate 프록시 객체 직렬화 문제 해결을 위한 설정
		Hibernate6Module hibernateModule = new Hibernate6Module();
		// 지연 로딩된 객체의 프록시를 무시하고 실제 객체만 직렬화. API 응답에 필요한 데이터가 누락될 수 있음.
		hibernateModule.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
		objectMapper.registerModule(hibernateModule);

		// 빈 객체 직렬화 시 예외 발생하지 않도록 설정
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		return objectMapper;
	}
}

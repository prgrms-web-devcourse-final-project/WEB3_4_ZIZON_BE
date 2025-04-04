package com.ll.dopdang.global.converter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * LocalDateTime 유연한 변환을 위한 커스텀 Deserializer
 * 날짜만 있는 경우(yyyy-MM-dd)와 날짜+시간이 모두 있는 경우(yyyy-MM-dd'T'HH:mm:ss)를 모두 처리
 */
@JsonComponent
public class LocalDateTimeConverter extends JsonDeserializer<LocalDateTime> {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	@Override
	public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String dateString = p.getText().trim();

		try {
			// ISO 형식(yyyy-MM-dd'T'HH:mm:ss)으로 파싱 시도
			return LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
		} catch (DateTimeParseException e) {
			try {
				// 날짜만 있는 경우(yyyy-MM-dd) 처리
				LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
				return date.atStartOfDay(); // 시간을 00:00:00으로 설정
			} catch (DateTimeParseException ex) {
				throw new IllegalArgumentException(
					"날짜 형식이 올바르지 않습니다. 'yyyy-MM-dd' 또는 'yyyy-MM-dd'T'HH:mm:ss' 형식을 사용해주세요.", ex);
			}
		}
	}
}

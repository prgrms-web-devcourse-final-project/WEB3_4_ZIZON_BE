package com.ll.dopdang.global.config;

import org.springframework.context.annotation.Configuration;

/**
 * AppConfig
 */
@Configuration
public class AppConfig {
	/**
	 * 프론트 주소 가져오기
	 * @return {@link String}
	 */
	public static String getSiteFrontUrl() {
		return "http://localhost:3000";
	}
}

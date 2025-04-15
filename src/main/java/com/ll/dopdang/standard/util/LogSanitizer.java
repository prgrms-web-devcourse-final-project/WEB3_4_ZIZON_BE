package com.ll.dopdang.standard.util;

/**
 * 로그 인젝션 공격을 방지하기 위한 유틸리티 클래스
 */
public class LogSanitizer {

	/**
	 * 로그 인젝션 공격을 방지하기 위해 로그 입력값을 정화합니다.
	 * 개행 문자와 탭 문자를 언더스코어로 대체합니다.
	 *
	 * @param input 정화할 입력 문자열
	 * @return 정화된 문자열
	 */
	public static String sanitizeLogInput(String input) {
		if (input == null)
			return null;
		return input.replaceAll("[\n\r\t]", "_");
	}
}

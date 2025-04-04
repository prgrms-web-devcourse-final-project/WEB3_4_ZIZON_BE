package com.ll.dopdang.domain.project.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.Getter;

/**
 * 프로젝트 상태를 나타내는 열거형.
 * OPEN - 모집 중
 * IN_PROGRESS - 작업 진행 중
 * COMPLETED - 작업 완료 후 구매 확정
 * CANCELLED - 취소 또는 문제 해결
 * 프론트엔드에서 문자열로 요청하는 상태 값을 역직렬화하여 Enum으로 매핑할 수 있도록
 * {@link JsonCreator}를 사용한 from() 메서드를 제공한다.
 */
@Getter
public enum ProjectStatus {
	OPEN("open"),
	IN_PROGRESS("in_progress"),
	COMPLETED("completed"),
	CANCELLED("cancelled");

	private final String value;

	ProjectStatus(String value) {
		this.value = value;
	}

	/**
	 * 문자열 값을 Enum으로 변환하는 메서드.
	 * 프론트에서 요청 시 status=open 같은 값이 들어올 때 이 메서드를 통해 Enum으로 매핑된다.
	 *
	 * @param value 문자열 형태의 상태 값
	 * @return 해당하는 ProjectStatus Enum
	 * @throws ServiceException 잘못된 값이 들어왔을 경우 예외 발생
	 */
	@JsonCreator
	public static ProjectStatus from(String value) {
		return Arrays.stream(ProjectStatus.values())
			.filter(e -> e.getValue().equalsIgnoreCase(value))
			.findFirst()
			.orElseThrow(() -> new ServiceException(ErrorCode.INVALID_PROJECT_STATUS));
	}
}

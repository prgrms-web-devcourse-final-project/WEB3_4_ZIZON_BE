package com.ll.dopdang.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "프로젝트 생성 응답 DTO")
public class ProjectCreateResponse {

	@Schema(description = "생성된 프로젝트 ID", example = "42")
	private Long projectId;

	@Schema(description = "응답 메시지", example = "프로젝트가 성공적으로 생성되었습니다. (ID: 42)")
	private String message;

}

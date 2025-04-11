package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.project.entity.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListForAllResponse {

	private Long id;
	private Long categoryId;
	private String title;
	private String summary;
	private String region;
	private BigDecimal budget;
	private String status;
	private LocalDateTime deadline;

	private String clientName;
	private String clientProfileImageUrl;

	private String thumbnailImageUrl;

	/**
	 * 정적 팩토리 메서드 - 전체 사용자용 프로젝트 요약 응답
	 */
	public static ProjectListForAllResponse from(Project project, String thumbnailImageUrl) {
		Member client = project.getClient();

		return ProjectListForAllResponse.builder()
			.id(project.getId())
			.categoryId(project.getCategory().getId())
			.title(project.getTitle())
			.summary(project.getSummary())
			.region(project.getRegion())
			.budget(project.getBudget())
			.status(project.getStatus().name())
			.deadline(project.getDeadline())
			.clientName(client.getName())
			.clientProfileImageUrl(client.getProfileImage())
			.thumbnailImageUrl(thumbnailImageUrl)
			.build();
	}
}

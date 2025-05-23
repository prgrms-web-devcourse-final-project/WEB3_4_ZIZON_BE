package com.ll.dopdang.domain.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
public class ProjectDetailResponse {
	//	프로젝트 단건 조회(전체이용)
	private Long id;
	private Long categoryId;
	private String title;
	private String summary;
	private String description;
	private String region;
	private BigDecimal budget;
	private LocalDateTime deadline;
	private String status;

	private String clientName;
	private String clientProfileImageUrl;

	private List<String> imageUrls;

	private String emails;

	// 이 방법!!
	public static ProjectDetailResponse of(Project project, List<String> imageUrls) {
		Member client = project.getClient();

		return ProjectDetailResponse.builder()
			.id(project.getId())
			.categoryId(project.getCategory().getId())
			.title(project.getTitle())
			.summary(project.getSummary())
			.description(project.getDescription())
			.region(project.getRegion())
			.budget(project.getBudget())
			.deadline(project.getDeadline())
			.status(project.getStatus().name())
			.clientName(client.getName())
			.clientProfileImageUrl(client.getProfileImage())
			.imageUrls(imageUrls)
			.emails(project.getClient().getEmail())
			.build();
	}
}

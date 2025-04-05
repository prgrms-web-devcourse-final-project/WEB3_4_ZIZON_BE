package com.ll.dopdang.domain.project.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ll.dopdang.domain.project.entity.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectListForAllPageResponse {

	private List<ProjectListForAllResponse> projects;
	private int currentPage;
	private int pageSize;
	private boolean hasNext;

	/**
	 * 전체 사용자용 페이지 응답 팩토리 메서드
	 *
	 * @param page         JPA에서 가져온 페이지 객체
	 * @param thumbnailMap 프로젝트 ID → 썸네일 이미지 URL
	 * @return 전체 사용자용 프로젝트 페이지 응답 DTO
	 */
	public static ProjectListForAllPageResponse from(Page<Project> page,
		java.util.Map<Long, String> thumbnailMap) {

		List<ProjectListForAllResponse> content = page.getContent().stream()
			.map(project -> {
				String thumbnailUrl = thumbnailMap.get(project.getId());

				// 썸네일이 없을 경우 클라이언트 프로필 이미지로 대체
				if (thumbnailUrl == null && project.getClient() != null) {
					thumbnailUrl = project.getClient().getProfileImage();
				}

				return ProjectListForAllResponse.from(project, thumbnailUrl);
			})
			.toList();

		return ProjectListForAllPageResponse.builder()
			.projects(content)
			.currentPage(page.getNumber())
			.pageSize(page.getSize())
			.hasNext(page.hasNext())
			.build();
	}
}

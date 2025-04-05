package com.ll.dopdang.domain.project.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.project.entity.ProjectImage;
import com.ll.dopdang.domain.project.repository.ProjectImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectImageService {

	private final ProjectImageRepository projectImageRepository;

	/**
	 * 모든 프로젝트의 대표 이미지를 맵으로 반환합니다.
	 * 대표 이미지는 프로젝트별 가장 orderNum이 낮은 이미지(=첫 번째 이미지)입니다.
	 *
	 * @return 프로젝트 ID → 대표 이미지 URL 맵
	 */
	public Map<Long, String> getThumbnails() {
		// 모든 이미지 불러와서 프로젝트별 첫 번째 이미지를 대표 이미지로 설정
		List<ProjectImage> allImages = projectImageRepository.findAllByOrderByProjectIdAscOrderNumAsc();

		// 프로젝트 ID 기준으로 첫 번째 이미지만 저장
		Map<Long, String> thumbnailMap = new LinkedHashMap<>();
		for (ProjectImage image : allImages) {
			Long projectId = image.getProject().getId();
			thumbnailMap.computeIfAbsent(projectId, id -> image.getImageUrl());
		}

		return thumbnailMap;
	}
}

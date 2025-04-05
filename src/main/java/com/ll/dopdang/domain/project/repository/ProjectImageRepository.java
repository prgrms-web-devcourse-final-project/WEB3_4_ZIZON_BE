package com.ll.dopdang.domain.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.project.entity.ProjectImage;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

	// 특정 프로젝트에 연관된 이미지 전체 조회 (순서 포함)
	List<ProjectImage> findByProjectIdOrderByOrderNumAsc(Long projectId);

	// 프로젝트별 대표 이미지를 구하기 위한 전체 이미지 조회 (정렬 포함)
	List<ProjectImage> findAllByOrderByProjectIdAscOrderNumAsc();
}

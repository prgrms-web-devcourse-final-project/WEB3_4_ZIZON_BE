package com.ll.dopdang.domain.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.project.entity.Project;
import com.ll.dopdang.domain.project.entity.ProjectImage;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

	// 특정 프로젝트에 연관된 이미지 전체 조회 (순서 포함)
	List<ProjectImage> findAllByProjectOrderByOrderNumAsc(Project project);

}

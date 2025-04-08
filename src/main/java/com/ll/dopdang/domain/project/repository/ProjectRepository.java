package com.ll.dopdang.domain.project.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.project.entity.Project;
import com.ll.dopdang.domain.project.entity.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	// 클라이언트가 등록한 모든 프로젝트
	Page<Project> findByClientId(Long clientId, Pageable pageable);

	// 클라이언트가 등록한 특정 상태의 프로젝트
	Page<Project> findByClientIdAndStatus(Long clientId, ProjectStatus status, Pageable pageable);

	@NotNull Optional<Project> findById(@NotNull Long id);

	@EntityGraph(attributePaths = {"client"})
	Optional<Project> findWithClientById(Long id);
}

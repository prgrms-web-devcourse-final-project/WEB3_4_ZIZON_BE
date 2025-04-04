package com.ll.dopdang.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}

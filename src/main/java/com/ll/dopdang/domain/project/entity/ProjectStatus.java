package com.ll.dopdang.domain.project.entity;

import lombok.Getter;

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

}

package com.ll.dopdang.domain.project.dto;

public record ProjectWithClientEmailResponse(
	Long id,
	String title,
	String summary,
	String region,
	String clientEmail
) {
}

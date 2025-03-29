package com.ll.dopdang.domain.member.entity;

public enum MemberRole {
	ROLE_CLIENT("일반 사용자"),
	ROLE_EXPERT("전문가"),
	ROLE_ADMIN("관리자");

	private final String description;

	MemberRole(String description) {
		this.description = description;
	}
}

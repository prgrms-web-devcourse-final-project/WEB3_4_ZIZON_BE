package com.ll.dopdang.domain.member.entity;

/**
 * 유저 역할 enum
 */
public enum MemberRole {
	/**
	 * 유저 역할
	 */
	ROLE_CLIENT("일반 사용자"),
	ROLE_EXPERT("전문가"),
	ROLE_ADMIN("관리자");

	private final String description;

	MemberRole(String description) {
		this.description = description;
	}
}

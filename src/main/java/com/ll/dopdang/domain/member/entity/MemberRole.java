package com.ll.dopdang.domain.member.entity;

/**
 * 유저 역할 enum
 */
public enum MemberRole {
	/**
	 * 유저 역할
	 */
	client("일반 사용자"),
	expert("전문가"),
	admin("관리자");

	private final String description;

	MemberRole(String description) {
		this.description = description;
	}
}

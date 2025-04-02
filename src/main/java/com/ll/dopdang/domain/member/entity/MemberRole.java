package com.ll.dopdang.domain.member.entity;

import lombok.Getter;

/**
 * 유저 역할 enum
 */
@Getter
public enum MemberRole {
	/**
	 * 유저 역할
	 */
	CLIENT("일반 사용자"),
	EXPERT("전문가"),
	ADMIN("관리자");

	private final String description;

	MemberRole(String description) {
		this.description = description;
	}

}

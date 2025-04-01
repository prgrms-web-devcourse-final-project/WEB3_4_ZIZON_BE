package com.ll.dopdang.domain.member.entity;

/**
 * 유저 상태 enum
 */
public enum MemberStatus {
	/**
	 * 유저 상태
	 */
	ACTIVE("활성"),
	UNCERIFIED("미인증"),
	DORMANT("휴먼"),
	SUSPENDED("정지"),
	DEACTIVATED("탈퇴");

	private final String description;

	MemberStatus(String description) {
		this.description = description;
	}
}

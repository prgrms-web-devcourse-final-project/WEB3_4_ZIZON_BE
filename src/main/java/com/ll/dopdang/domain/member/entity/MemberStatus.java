package com.ll.dopdang.domain.member.entity;

/**
 * 유저 상태 enum
 */
public enum MemberStatus {
	/**
	 * 유저 상태
	 */
	활성("활성"),
	미인증("미인증"),
	휴면("휴먼"),
	정지("정지"),
	탈퇴("탈퇴");

	private final String description;

	MemberStatus(String description) {
		this.description = description;
	}
}

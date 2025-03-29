package com.ll.dopdang.domain.member.entity;

public enum MemberStatus {
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

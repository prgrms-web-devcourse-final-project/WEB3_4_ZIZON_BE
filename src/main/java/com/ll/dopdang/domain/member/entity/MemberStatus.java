package com.ll.dopdang.domain.member.entity;

import lombok.Getter;

/**
 * 유저 상태 enum
 */
@Getter
public enum MemberStatus {
	/**
	 * 유저 상태
	 */
	ACTIVE("활성"),
	UNVERIFIED("미인증"),
	DORMANT("휴먼"),
	SUSPENDED("정지"),
	DEACTIVATED("탈퇴");

	private final String description;

	MemberStatus(String description) {
		this.description = description;
	}

}

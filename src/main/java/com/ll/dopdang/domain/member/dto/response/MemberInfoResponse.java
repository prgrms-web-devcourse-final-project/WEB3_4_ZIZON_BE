package com.ll.dopdang.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberInfoResponse {
	private Long id;
	private String email;
	private String name;
}

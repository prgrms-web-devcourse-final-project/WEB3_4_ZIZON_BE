package com.ll.dopdang.domain.member.service;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	public Member getMemberById(Long id) {
		return memberRepository.findById(id)
			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));
	}
}

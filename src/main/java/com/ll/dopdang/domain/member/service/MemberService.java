package com.ll.dopdang.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * MemberService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
}

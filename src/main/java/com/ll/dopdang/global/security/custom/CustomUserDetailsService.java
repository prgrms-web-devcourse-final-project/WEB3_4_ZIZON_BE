package com.ll.dopdang.global.security.custom;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberStatus;
import com.ll.dopdang.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

/**
 * CustomUserDetailsService
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	/**
	 * 유저 repository
	 */
	private final MemberRepository memberRepository;

	/**
	 *
	 * @param email 이메일
	 * @return {@link UserDetails}
	 * @throws UsernameNotFoundException 예외
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

		if (MemberStatus.DEACTIVATED.toString().equals(member.getStatus())) {
			throw new UsernameNotFoundException("탈퇴한 계정입니다.");
		}

		return new CustomUserDetails(member);
	}
}

package com.ll.dopdang.global.security.custom;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberStatus;

import lombok.RequiredArgsConstructor;

/**
 * CustomUserDetails
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	/**
	 * 유저
	 */
	private final Member member;

	/**
	 * 유저 역할 가져오기
	 * @return {@link Collection}
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		authorities.add(new SimpleGrantedAuthority(member.getUserRole()));

		return authorities;
	}

	/**
	 * 유저 가져오기
	 * @return {@link Member}
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * 유저 ID 가져오기
	 * @return {@link Long}
	 */
	public Long getId() {
		return member.getId();
	}

	/**
	 * 유저 이메일 가져오기
	 * @return {@link String}
	 */
	@Override
	public String getUsername() {
		return member.getEmail();
	}

	/**
	 * 유저 비밀번호 가져오기
	 * @return {@link String}
	 */
	@Override
	public String getPassword() {
		return member.getPassword();
	}

	/**
	 *
	 * @return {@link Boolean}
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 *
	 * @return {@link Boolean}
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 *
	 * @return {@link Boolean}
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 *
	 * @return {@link Boolean}
	 */
	@Override
	public boolean isEnabled() {
		return !MemberStatus.탈퇴.toString().equals(member.getStatus());
	}

	/**
	 *
	 * @return {@link String}
	 */
	@Override
	public String toString() {
		return "CustomUserDetails{" + "member=" + (member != null
			? "Member(id=" + member.getId() + ", email=" + member.getEmail() + ")" : "null") + "}";
	}
}

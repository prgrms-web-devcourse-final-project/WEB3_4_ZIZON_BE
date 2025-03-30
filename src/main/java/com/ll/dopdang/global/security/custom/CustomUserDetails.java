package com.ll.dopdang.global.security.custom;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
	private final Member member;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		authorities.add(new SimpleGrantedAuthority(member.getUserRole()));

		return authorities;
	}

	public Member getMember() {
		return member;
	}

	public Long getId() {
		return member.getId();
	}

	@Override
	public String getUsername() {
		return member.getEmail();
	}

	@Override
	public String getPassword() {
		return member.getPassword();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return MemberStatus.ACTIVE.toString().equals(member.getStatus());
	}

	@Override
	public String toString() {
		return "CustomUserDetails{" + "member=" + (member != null
			? "Member(id=" + member.getId() + ", email=" + member.getEmail() + ")" : "null") + "}";
	}
}

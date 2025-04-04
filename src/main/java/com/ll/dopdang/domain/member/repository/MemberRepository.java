package com.ll.dopdang.domain.member.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.dopdang.domain.member.entity.Member;

/**
 * MemberRepository
 */
public interface MemberRepository extends JpaRepository<Member, Long> {
	/**
	 * 이메일을 통한 유저 검색
	 * @param email 이메일
	 * @return {@link Optional<Member>}
	 */
	Optional<Member> findByEmail(String email);

	/**
	 * 소셜 ID를 통한 유저 검색
	 * @param memberId 소셜 ID
	 * @return {@link Optional<Member>}
	 */
	Optional<Member> findByMemberId(String memberId);

	/**
	 * ID를 통한 유저 검색
	 * @param id ID
	 * @return {@link Optional<Member>}
	 */
	@Override
	@NotNull Optional<Member> findById(@NotNull Long id);
}

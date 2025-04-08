package com.ll.dopdang.domain.member.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.ObjectUtils;

import com.ll.dopdang.domain.expert.entity.Expert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Member 엔티티
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Member {
	/**
	 * 유저 고유 ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 이메일
	 */
	@Email
	@Column(unique = true, nullable = false)
	private String email;

	/**
	 * 비밀번호
	 */
	@Column(nullable = false)
	private String password;

	/**
	 * 이름
	 */
	@Column(nullable = false)
	private String name;

	/**
	 * 전화번호
	 */
	private String phone;

	/**
	 * 프로필 사진
	 */
	@Column(name = "profile_image")
	private String profileImage;

	/**
	 * 생성 일자
	 */
	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * 수정 일자
	 */
	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/**
	 * 유저 상태
	 */
	@Column(nullable = false)
	private String status;

	/**
	 * 유저 역할
	 */
	@Column(name = "user_role")
	private String userRole;

	/**
	 * 유저 소셜 ID
	 */
	@Setter(AccessLevel.PRIVATE)
	@Column(name = "member_id", nullable = false)
	private String memberId;

	/**
	 * 사용자 고유 번호
	 */
	@Column(name = "unique_key", nullable = false, updatable = false, unique = true)
	private String uniqueKey;

	/**
	 * 현재 사용자의 상태 확인 (true = 의뢰자, false = 전문가)
	 */
	@Column(name = "is_client", nullable = false)
	private boolean isClient;

	@OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
	private Expert expert;

	/**
	 * 유저 생성 시, 자동으로 uniqueKey를 생성해주는 메서드
	 */
	@PrePersist
	protected void onCreate() {
		if (ObjectUtils.isEmpty(uniqueKey)) {
			this.uniqueKey = UUID.randomUUID().toString();
		}
		this.isClient = true;
	}

	/**
	 * 유저를 활성화 상태로 설정하는 메서드
	 */
	public void activateMember() {
		this.status = MemberStatus.ACTIVE.toString();
	}

	/**
	 * 유저의 전화번호를 설정하는 메서드
	 * @param phone 전화번호
	 */
	public void activatePhone(String phone) {
		this.phone = phone;
	}

}

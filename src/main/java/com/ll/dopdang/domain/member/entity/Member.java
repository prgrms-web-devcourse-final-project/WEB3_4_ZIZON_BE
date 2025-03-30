package com.ll.dopdang.domain.member.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Email
	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String phone;

	@Column(name = "profile_image")
	private String profileImage;

	@CreatedDate
	@Column(name = "create_at")
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(nullable = false)
	private String status;

	@Column(name = "user_role")
	private String userRole;

	@Setter(AccessLevel.PRIVATE)
	@Column(name = "member_id", nullable = false)
	private String memberId;

	@Column(name = "unique_key", nullable = false, updatable = false, unique = true)
	private String uniqueKey;

	@PrePersist
	protected void onCreate() {
		if (this.uniqueKey == null) {
			this.uniqueKey = UUID.randomUUID().toString();
		}
	}
}

package com.ll.dopdang.domain.project.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private Member client;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	private BigDecimal budget;

	private LocalDateTime deadline;

	@Enumerated(EnumType.STRING)
	private ProjectStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "expert_id")
	private Expert expert;

	// 생성자, 비즈니스 로직 등...
}

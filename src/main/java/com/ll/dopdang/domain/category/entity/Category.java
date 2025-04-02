package com.ll.dopdang.domain.category.entity;

import java.util.LinkedHashSet;
import java.util.Set;

import com.ll.dopdang.domain.member.entity.Expert;
import com.ll.dopdang.domain.project.entity.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "parent_id")
	private Long parentId;

	@Size(max = 100)
	@NotNull
	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@NotNull
	@Column(name = "level", nullable = false)
	private Byte level;

	/**
	 * 카테고리 유형 (PROJECT, PRODUCT, PROVISION)
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "category_type", nullable = false)
	private CategoryType categoryType;

	@OneToMany(mappedBy = "categoryId")
	private Set<Expert> experts = new LinkedHashSet<>();

	@OneToMany(mappedBy = "category")
	private Set<Project> projects = new LinkedHashSet<>();

}

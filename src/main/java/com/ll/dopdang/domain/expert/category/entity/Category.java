package com.ll.dopdang.domain.expert.category.entity;

import com.ll.dopdang.domain.project.entity.Project;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

	@Id
	private long id;

	@Column(nullable = false, length = 100)
	private String name;

	private boolean level = false;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Category parent;

	@OneToMany(mappedBy = "subCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ExpertCategory> expertCategories = new ArrayList<>(); // ExpertCategory와의 1:N 관계

	@Enumerated(EnumType.STRING)
	@Column(name = "category_type", nullable = false, length = 50)
	private CategoryType categoryType;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Project> projects = new ArrayList<>();

}

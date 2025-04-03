package com.ll.dopdang.domain.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

	@Id
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Category parent;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private boolean level;

	@Enumerated(EnumType.STRING)
	@Column(name = "category_type", nullable = false, length = 50)
	private CategoryType categoryType;

}


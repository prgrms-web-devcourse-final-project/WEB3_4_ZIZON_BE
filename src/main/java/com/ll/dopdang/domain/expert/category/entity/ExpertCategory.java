package com.ll.dopdang.domain.expert.category.entity;

import com.ll.dopdang.domain.expert.entity.Expert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 연결 엔티티 고유 ID

	@ManyToOne(optional = false) // 전문가와 N:1 관계
	@JoinColumn(name = "expert_id", nullable = false) // 외래키: 전문가 ID
	private Expert expert;

	@ManyToOne(optional = false) // 소분류와 N:1 관계
	@JoinColumn(name = "sub_category_id", nullable = false) // 외래키: 소분류 카테고리 ID
	private Category subCategory;

}

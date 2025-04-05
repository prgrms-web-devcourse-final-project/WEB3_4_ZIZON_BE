package com.ll.dopdang.domain.expert.entity;

import java.util.ArrayList;
import java.util.List;

import com.ll.dopdang.domain.expert.category.entity.Category;
import com.ll.dopdang.domain.expert.category.entity.ExpertCategory;
import com.ll.dopdang.domain.member.entity.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 전문가 고유 ID

	@OneToOne(optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member; // 해당 전문가와 연결된 회원 정보

	@ManyToOne(optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category; // 전문가의 대분류 카테고리

	@OneToMany(mappedBy = "expert", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ExpertCategory> subCategories = new ArrayList<>(); // ExpertCategory와의 1:N 관계

	@Column(length = 200)
	private String introduction; // 자기소개

	@Column(nullable = false)
	private int careerYears; // 경력 연수

	@Column(length = 200)
	private String certification; // 자격증 정보 (Optional)

	@Column(nullable = false)
	private Boolean gender; // 0 = 남자, 1 = 여자

	@Column(nullable = false, length = 100)
	private String bankName; // 은행명

	@Column(nullable = false, length = 100)
	private String accountNumber; // 계좌번호

	@Column
	private boolean Availability = false;// 활동 가능 여부

	@Column(length = 300)
	private String sellerInfo; // 판매자 관련 정보 (Optional
}

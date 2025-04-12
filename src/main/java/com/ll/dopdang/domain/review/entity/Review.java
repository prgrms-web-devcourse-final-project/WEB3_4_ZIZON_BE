package com.ll.dopdang.domain.review.entity;

import java.math.BigDecimal;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.project.entity.Contract;
import com.ll.dopdang.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, precision = 3, scale = 2) // 5.00까지 저장
	private BigDecimal score;

	@Column(nullable = false, length = 1000)
	private String content; // 리뷰 내용

	// [선택] 이미지 URL 또는 업로드 처리에 따라
	private String imageUrl;

	// 작성자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id", referencedColumnName = "id")
	private Member reviewer;

	@Column(nullable = false)
	private boolean deleted = false;

	// -------- 연결 관계 --------

	// 하나만 사용하고 나머지는 null로 두는 방식
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contract_id")
	private Contract contract;

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "order_id")
	// private Order order;

	public static Review fromContract(Contract contract, Member reviewer, BigDecimal score, String content,
		String imageUrl) {
		Review review = new Review();
		review.contract = contract;
		review.reviewer = reviewer;
		review.score = score;
		review.content = content;
		review.imageUrl = imageUrl;
		return review;
	}

	public void markAsDeleted() {
		this.deleted = true;
	}

	// public static Review fromOrder(Order order, Member reviewer, BigDecimal score, String content, String imageUrl) {
	// 	Review review = new Review();
	// 	review.order = order;
	// 	review.reviewer = reviewer;
	// 	review.score = score;
	// 	review.content = content;
	// 	review.imageUrl = imageUrl;
	// 	return review;
	// }
}

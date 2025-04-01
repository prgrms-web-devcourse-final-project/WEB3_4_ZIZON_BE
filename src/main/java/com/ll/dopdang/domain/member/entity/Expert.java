package com.ll.dopdang.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 로직에 필요한 데이터를 생성하기 위해 임시로 작성한 엔티티
 */
@Entity
@Getter
@Table(name = "expert")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Expert {

	@Id
	private Long id;

	@OneToOne
	@MapsId  // member_id를 PK로 사용
	@JoinColumn(name = "id")
	private Member member;

	private Long categoryId; // 추후 수정 필요

	@Column(columnDefinition = "TEXT")
	private String introduction;

	private Integer careerYears;

	private String certification;

	private String bankName;

	private String accountNumber;

	private Boolean availability;

	@Column(columnDefinition = "TEXT")
	private String sellerInfo;
}

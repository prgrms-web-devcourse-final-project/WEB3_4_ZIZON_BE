package com.ll.dopdang.domain.expert.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertCertificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 자동 생성 ID

	@ManyToOne(optional = false)
	@JoinColumn(name = "expert_id")
	private Expert expert; // Expert 엔티티와 연결

	@ManyToOne(optional = false)
	@JoinColumn(name = "certificate_id")
	private Certificate certificate; // Certificate 엔티티와 연결
}
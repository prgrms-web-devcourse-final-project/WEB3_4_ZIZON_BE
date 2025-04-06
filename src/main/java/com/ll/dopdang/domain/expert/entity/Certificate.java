package com.ll.dopdang.domain.expert.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Certificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 자동 생성 ID

	private String name; // 자격증 이름

	@OneToMany(mappedBy = "certificate", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExpertCertificate> expertCertificates = new ArrayList<>();
}

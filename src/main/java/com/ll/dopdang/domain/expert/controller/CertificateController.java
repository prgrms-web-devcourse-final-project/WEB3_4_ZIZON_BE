package com.ll.dopdang.domain.expert.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.expert.service.CertificateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/certificates") // 공통 URL 경로
@RequiredArgsConstructor
public class CertificateController {
	private final CertificateService certificateService;

	// GET 요청으로 데이터 수집 및 저장 테스트
	@GetMapping
	public String fetchAndSaveCertificates() {
		try {
			certificateService.fetchAndSaveCertificates(); // 서비스 호출
			return "자격증 데이터를 성공적으로 저장했습니다!";
		} catch (Exception e) {
			return "오류 발생: " + e.getMessage();
		}
	}
}
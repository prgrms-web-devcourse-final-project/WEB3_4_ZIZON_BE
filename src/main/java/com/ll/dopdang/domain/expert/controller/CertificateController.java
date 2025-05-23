package com.ll.dopdang.domain.expert.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.expert.dto.response.CertificateResponseDto;
import com.ll.dopdang.domain.expert.service.CertificateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/certificates") // 공통 URL 경로
@RequiredArgsConstructor
public class CertificateController {
	private final CertificateService certificateService;

	/**
	 * 모든 Certificate 데이터 조회 API
	 */
	@GetMapping
	public ResponseEntity<List<CertificateResponseDto>> getAllCertificates() {
		List<CertificateResponseDto> certificates = certificateService.getAllCertificates();
		return new ResponseEntity<>(certificates, HttpStatus.OK);
	}

	@GetMapping("/search")
	public ResponseEntity<List<CertificateResponseDto>> searchCertificates(@RequestParam String name) {
		List<CertificateResponseDto> certificates = certificateService.getCertificatesByName(name);
		return new ResponseEntity<>(certificates, HttpStatus.OK);
	}

	// GET 요청으로 데이터 수집 및 저장 테스트
	@GetMapping("/onlyForTest")
	public String fetchAndSaveCertificates() {
		try {
			certificateService.fetchAndSaveCertificates(); // 서비스 호출
			return "자격증 데이터를 성공적으로 저장했습니다!";
		} catch (Exception e) {
			return "오류 발생: " + e.getMessage();
		}
	}
}
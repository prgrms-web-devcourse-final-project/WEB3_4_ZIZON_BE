package com.ll.dopdang.domain.expert.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CertificateScheduler {
	private final CertificateService certificateService;

	@Scheduled(cron = "0 0 17 * * SAT") // 매주 월요일 오전 3시 실행
	public void updateCertificates() {
		certificateService.fetchAndSaveCertificates();
	}
}

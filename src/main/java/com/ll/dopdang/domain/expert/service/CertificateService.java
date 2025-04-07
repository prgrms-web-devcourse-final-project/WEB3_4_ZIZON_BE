package com.ll.dopdang.domain.expert.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.ll.dopdang.domain.expert.dto.response.CertificateResponseDto;
import com.ll.dopdang.domain.expert.entity.Certificate;
import com.ll.dopdang.domain.expert.repository.CertificateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CertificateService {
	private static final Logger log = LoggerFactory.getLogger(CertificateService.class);
	private static final String API_URL = "http://openapi.q-net.or.kr/api/service/rest/InquiryQualInfo/getList";
	private final CertificateRepository certificateRepository; // DB 접근 레포지토리
	@Value("${service-key}") // 환경변수로부터 서비스 키 값 주입
	private String serviceKey;

	public List<CertificateResponseDto> getAllCertificates() {
		// 데이터베이스에서 모든 Certificate 조회
		List<Certificate> certificates = certificateRepository.findAll();

		// Certificate 데이터를 CertificateResponseDto로 매핑
		return certificates.stream()
			.map(certificate -> CertificateResponseDto.builder()
				.id(certificate.getId())
				.name(certificate.getName())
				.build())
			.toList();
	}


	public void fetchAndSaveCertificates() {
		try {
			// 1. URL 생성 및 API 호출
			String url = API_URL + "?serviceKey=" + serviceKey;
			url += "&seriesCd=01"; // 카테고리 필터 01 기술사, 02 기능장, 03 기사, 04 기능사
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
			String xmlResponse = restTemplate.getForObject(url, String.class);

			if (xmlResponse == null || xmlResponse.isBlank()) {
				return;
			}

			// 2. XML 데이터 파싱
			XmlMapper xmlMapper = new XmlMapper();
			var root = xmlMapper.readTree(xmlResponse);
			var items = root.path("body").path("items").findValues("jmNm");

			// 3. 기존 DB 데이터 조회
			List<String> existingNames = certificateRepository.findAll()
				.stream()
				.map(Certificate::getName)
				.toList();
			HashSet<String> existingNameSet = new HashSet<>(existingNames);

			// 4. 새로운 자격증만 배치로 추가
			List<Certificate> certificatesToSave = new ArrayList<>();
			for (var item : items) {
				String name = item.asText();
				if (!name.isBlank() && !existingNameSet.contains(name)) {
					Certificate certificate = new Certificate();
					certificate.setName(name);
					certificatesToSave.add(certificate);
					existingNameSet.add(name); // 중복 방지
				}
			}

			if (!certificatesToSave.isEmpty()) {
				certificateRepository.saveAll(certificatesToSave); // 배치 저장
			}

		} catch (Exception e) {
		}
	}

}
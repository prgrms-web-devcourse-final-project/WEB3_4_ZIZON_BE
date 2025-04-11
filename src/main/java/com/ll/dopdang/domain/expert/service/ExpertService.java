package com.ll.dopdang.domain.expert.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.expert.category.entity.Category;
import com.ll.dopdang.domain.expert.category.entity.ExpertCategory;
import com.ll.dopdang.domain.expert.category.repository.CategoryRepository;
import com.ll.dopdang.domain.expert.category.repository.ExpertCategoryRepository;
import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.request.ExpertUpdateRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertDetailResponseDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.entity.Certificate;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.entity.ExpertCertificate;
import com.ll.dopdang.domain.expert.entity.Portfolio;
import com.ll.dopdang.domain.expert.repository.CertificateRepository;
import com.ll.dopdang.domain.expert.repository.ExpertCertificateRepository;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.expert.repository.PortfolioRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 전문가 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class ExpertService {

	private final ExpertRepository expertRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;
	private final CertificateRepository certificateRepository;
	private final ExpertCategoryRepository expertCategoryRepository;
	private final ExpertCertificateRepository expertCertificateRepository;
	private final PortfolioRepository portfolioRepository;

	/**
	 * 전문가를 등록합니다.
	 *
	 * @param expertRequestDto 전문가 정보를 담은 DTO.
	 * @param memberId 전문가가 될 회원 ID.
	 * @throws IllegalArgumentException 회원 또는 카테고리가 존재하지 않을 경우 예외 발생.
	 */
	@Transactional
	public Long createExpert(ExpertRequestDto expertRequestDto, Long memberId) throws Exception {
		// 1. 회원 조회 및 검증
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new IllegalArgumentException("Member not found"));

		// 2. 대분류 카테고리 조회 및 검증
		Category category = categoryRepository.findByNameAndParentIsNull(expertRequestDto.getCategoryName())
			.orElseThrow(() -> new IllegalArgumentException("Main category not found"));

		// 3. 소분류 카테고리 조회 및 검증
		List<Category> subCategories = expertRequestDto.getSubCategoryNames().stream()
			.map(subCategoryName -> categoryRepository.findByNameAndParent(subCategoryName, category)
				.orElseThrow(() -> new IllegalArgumentException("Subcategory not found: " + subCategoryName)))
			.toList();

		List<Certificate> certificates = expertRequestDto.getCertificateNames().stream()
			.map(certificateName -> certificateRepository.findByName(certificateName)
				.orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + certificateName)))
			.toList();
		// 4. Expert 엔티티 생성 및 저장
		Expert expert = Expert.builder()
			.member(member)
			.category(category)
			.careerYears(expertRequestDto.getCareerYears())
			.introduction(expertRequestDto.getIntroduction())
			.bankName(expertRequestDto.getBankName())
			.gender(expertRequestDto.getGender())
			.accountNumber(expertRequestDto.getAccountNumber())
			.build();
		expertRepository.save(expert);

		// 5. ExpertCategory 엔티티 생성 및 저장
		subCategories.forEach(subCategory -> {
			ExpertCategory expertCategory = ExpertCategory.builder()
				.expert(expert)
				.subCategory(subCategory)
				.build();

			expertCategoryRepository.save(expertCategory);
		});
		certificates.forEach(certificate -> {
			ExpertCertificate expertCertificate = ExpertCertificate.builder()
				.expert(expert)
				.certificate(certificate)
				.build();
			expertCertificateRepository.save(expertCertificate);
		});
			// 새로운 포트폴리오 생성
			Portfolio portfolio = Portfolio.builder()
				.expert(expert)
				.title(expertRequestDto.getPortfolioTitle() != null ? expertRequestDto.getPortfolioTitle() : "") // 기본값 설정
				.imageUrl(expertRequestDto.getPortfolioImage() != null ? expertRequestDto.getPortfolioImage() : "") // 기본값: 빈 URL
				.build();
			// 새로 생성한 포트폴리오 저장
			portfolioRepository.save(portfolio);
		return expert.getId();
	}

	/**
	 * 전문가 목록을 조회합니다.
	 *
	 * @param categoryNames 필터링할 카테고리 이름 리스트.
	 * @param careerLevel 필터링할 경력 수준 (junior, senior, expert).
	 * @return 전문가 목록을 담은 DTO 리스트.
	 * @throws IllegalArgumentException 잘못된 careerLevel이 제공된 경우 예외 발생.
	 */
	public List<ExpertResponseDto> getAllExperts(List<String> categoryNames, String careerLevel) {
		Integer minYears = null;
		Integer maxYears = null;

		// careerLevel에 따른 경력 필터링 기준 설정
		if (Objects.nonNull(careerLevel)) {
			switch (careerLevel.toLowerCase()) {
				case "junior": // 1~5년
					minYears = 1;
					maxYears = 5;
					break;
				case "senior": // 6~10년
					minYears = 6;
					maxYears = 10;
					break;
				case "expert": // 11년 이상
					minYears = 11;
					maxYears = 100; // 제한 없는 최대값 설정
					break;
				default:
					throw new IllegalArgumentException("Invalid careerLevel: " + careerLevel);
			}
		}
		// 데이터베이스 조회를 통한 필터링 결과
	List<Expert> experts = expertRepository.findByFilters(categoryNames, minYears, maxYears);

		// Expert 데이터를 DTO로 변환하여 반환
		return experts.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	public List<ExpertResponseDto> searchExperts(List<String> categoryNames, String careerLevel, String name) {
		Integer minYears = null;
		Integer maxYears = null;

		// careerLevel에 따른 경력 필터링 기준 설정
		if (Objects.nonNull(careerLevel)) {
			switch (careerLevel.toLowerCase()) {
				case "junior": // 1~5년
					minYears = 1;
					maxYears = 5;
					break;
				case "senior": // 6~10년
					minYears = 6;
					maxYears = 10;
					break;
				case "expert": // 11년 이상
					minYears = 11;
					maxYears = 100; // 제한 없는 최대 값 설정
					break;
				default:
					throw new IllegalArgumentException("Invalid careerLevel: " + careerLevel);
			}
		}

		// 데이터베이스에서 조건에 맞는 전문가 조회
		List<Expert> experts = expertRepository.findByFilters(categoryNames, minYears, maxYears, name);

		// Expert 데이터를 DTO로 변환하여 반환
		return experts.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	public ExpertDetailResponseDto getExpertById(Long expertId) {
		// 1. 전문가 조회
		Expert expert = expertRepository.findById(expertId)
			.orElseThrow(() -> new IllegalArgumentException("Expert not found with ID: " + expertId));
		Portfolio portfolio = expert.getPortfolio();
		// 2. Expert -> ExpertDetailResponseDto로 변환
		return mapToDetailResponseDto(expert,portfolio);
	}

	public List<ExpertResponseDto> searchByName(String name) {
		List<Expert> experts = expertRepository.findByMemberNameContaining(name);
		return experts.stream()
			.map(this::mapToResponseDto)
			.toList();
	}

	/**
	 * 전문가 정보 업데이트 서비스 메서드
	 *
	 * @param expertId 전문가 ID
	 * @param updateRequestDto 업데이트 내용이 담긴 DTO
	 * @throws IllegalArgumentException 전문가 또는 카테고리를 찾을 수 없는 경우 예외 발생
	 */
	@Transactional
	public ExpertDetailResponseDto updateExpert(Long expertId, ExpertUpdateRequestDto updateRequestDto) {
		// 1. 전문가 조회
		Expert existingExpert = expertRepository.findById(expertId)
			.orElseThrow(() -> new IllegalArgumentException("Expert not found with ID: " + expertId));

		// 2. 대분류 카테고리 변경 처리
		Category category = existingExpert.getCategory(); // 기존 대분류
		if (updateRequestDto.getCategoryName() != null) {
			category = categoryRepository.findByNameAndParentIsNull(updateRequestDto.getCategoryName())
				.orElseThrow(() -> new IllegalArgumentException(
					"Main category not found: " + updateRequestDto.getCategoryName()));
		}
		existingExpert.setCategory(category);

		// 3. 소분류 카테고리 변경 처리
		if (updateRequestDto.getSubCategoryNames() != null && !updateRequestDto.getSubCategoryNames().isEmpty()) {
			// 기존 소분류 삭제
			expertCategoryRepository.deleteAllByExpertId(expertId);
			expertCategoryRepository.flush();

			// 새로운 소분류 생성 및 저장
			List<Category> subCategories = updateRequestDto.getSubCategoryNames().stream()
				.map(name -> categoryRepository.findByName(name)
					.orElseThrow(() -> new IllegalArgumentException("Subcategory not found: " + name)))
				.toList();

			subCategories.forEach(subCategory -> {
				ExpertCategory expertCategory = ExpertCategory.builder()
					.expert(existingExpert)
					.subCategory(subCategory)
					.build();
				expertCategoryRepository.save(expertCategory);
			});
			List<ExpertCertificate> expertCertificates = new ArrayList<>();
			// 4. 자격증 처리
			if (updateRequestDto.getCertificateNames() != null && !updateRequestDto.getCertificateNames().isEmpty()) {
				// 기존 자격증 삭제
				expertCertificateRepository.deleteAllByExpertId(expertId);
				expertCertificateRepository.flush();

				// 새로운 자격증 저장
				List<Certificate> certificates = updateRequestDto.getCertificateNames().stream()
					.map(name -> certificateRepository.findByName(name)
						.orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + name)))
					.toList();

				expertCertificates = certificates.stream()
					.map(certificate -> ExpertCertificate.builder()
						.expert(existingExpert)
						.certificate(certificate)
						.build()
					)
					.toList();

				expertCertificateRepository.saveAll(expertCertificates);
			}

			// 5. 기존 포트폴리오 삭제 및 새로 생성
			if (updateRequestDto.getPortfolioTitle() != null || updateRequestDto.getPortfolioImage() != null) {
				Portfolio existingPortfolio = portfolioRepository.findByExpertId(expertId).orElse(null);

				if (existingPortfolio != null) {
					portfolioRepository.delete(existingPortfolio); // 기존 포트폴리오 삭제
					portfolioRepository.flush();
				}

				Portfolio newPortfolio = Portfolio.builder()
					.expert(existingExpert) // Expert와 연결
					.title(updateRequestDto.getPortfolioTitle() != null ? updateRequestDto.getPortfolioTitle() : "Default Title")
					.imageUrl(updateRequestDto.getPortfolioImage() != null ? updateRequestDto.getPortfolioImage() : "")
					.build();

				portfolioRepository.save(newPortfolio);
				existingExpert.setPortfolio(newPortfolio);
			}

			// 6. 기타 데이터 업데이트
			existingExpert.setCareerYears(updateRequestDto.getCareerYears());
			existingExpert.setIntroduction(updateRequestDto.getIntroduction());
			existingExpert.setBankName(updateRequestDto.getBankName());
			existingExpert.setAccountNumber(updateRequestDto.getAccountNumber());

			// 7. 저장 후 반환
			return mapToDetailResponseDto(existingExpert, existingExpert.getPortfolio());
		}
		Expert expert = expertRepository.findById(expertId)
			.orElseThrow(() -> new IllegalArgumentException("Expert not found with ID: " + expertId));
		return mapToDetailResponseDto(expert,expert.getPortfolio());
	}

	/**
	 * Expert 엔티티를 ExpertResponseDto로 변환합니다.
	 */
	private ExpertResponseDto mapToResponseDto(Expert expert) {
		return ExpertResponseDto.builder()
			.expertId(expert.getId())
			.name(expert.getMember().getName()) // Member 이름
			.categoryName(expert.getCategory().getName())
			.careerYears(expert.getCareerYears())
			.introduction(expert.getIntroduction())
			.mainCategoryId(expert.getCategory().getId())
			.profileImage(expert.getMember().getProfileImage())
			.build();
	}

	/**
	 * Expert 엔티티 -> ExpertDetailResponseDto로 변환합니다.
	 */
	private ExpertDetailResponseDto mapToDetailResponseDto(Expert expert, Portfolio portfolio) {
		return ExpertDetailResponseDto.builder()
			.id(expert.getId())
			.mainCategoryId(expert.getCategory().getId())
			.name(expert.getMember().getName()) // Member 엔티티의 name
			.categoryName(expert.getCategory().getName())
			.subCategoryNames(expert.getSubCategories().stream()
				.map(sc -> sc.getSubCategory().getName())
				.toList())
			.subCategoryIds(expert.getSubCategories().stream()
				.map(sc -> sc.getSubCategory().getId())
				.toList())
			.introduction(expert.getIntroduction())
			.careerYears(expert.getCareerYears())
			.accountNumber(expert.getAccountNumber())
			.bankName(expert.getBankName())
			.profileImage(expert.getMember().getProfileImage())
			.gender(expert.getGender())
			.portfolioTitle(portfolio.getTitle())
			.portfolioImage(portfolio.getImageUrl())
			.certificateNames(expert.getExpertCertificates().stream() // 자격증 추가
				.map(expertCertificate -> expertCertificate.getCertificate().getName())
				.toList())
			.build();
	}
}

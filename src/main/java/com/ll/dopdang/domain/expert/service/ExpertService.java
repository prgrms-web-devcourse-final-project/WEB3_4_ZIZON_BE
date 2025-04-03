package com.ll.dopdang.domain.expert.service;

import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.entity.ExpertCategory;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import com.ll.dopdang.domain.category.repository.ExpertCategoryRepository;
import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertDetailResponseDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 전문가 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ExpertCategoryRepository expertCategoryRepository;

    /**
     * 전문가를 등록합니다.
     *
     * @param expertRequestDto 전문가 정보를 담은 DTO.
     * @param memberId 전문가가 될 회원 ID.
     * @throws IllegalArgumentException 회원 또는 카테고리가 존재하지 않을 경우 예외 발생.
     */
    @Transactional
    public void createExpert(ExpertRequestDto expertRequestDto, Long memberId) {
        // 1. 회원 조회 및 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 2. 대분류 카테고리 조회 및 검증
        Category mainCategory = categoryRepository.findByNameAndParentIsNull(expertRequestDto.getMainCategoryName())
                .orElseThrow(() -> new IllegalArgumentException("Main category not found"));

        // 3. 소분류 카테고리 조회 및 검증
        List<Category> subCategories = expertRequestDto.getSubCategoryNames().stream()
                .map(subCategoryName -> categoryRepository.findByNameAndParent(subCategoryName, mainCategory)
                        .orElseThrow(() -> new IllegalArgumentException("Subcategory not found: " + subCategoryName)))
                .collect(Collectors.toList());

        // 4. Expert 엔티티 생성 및 저장
        Expert expert = Expert.builder()
                .member(member)
                .mainCategory(mainCategory)
                .careerYears(expertRequestDto.getCareerYears())
                .introduction(expertRequestDto.getIntroduction())
                .certification(expertRequestDto.getCertification())
                .bankName(expertRequestDto.getBankName())
                .gender(expertRequestDto.getGender())
                .accountNumber(expertRequestDto.getAccountNumber())
                .sellerInfo(expertRequestDto.getSellerInfo())
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
    public ExpertDetailResponseDto getExpertById(Long expertId) {
        // 1. 전문가 조회
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new IllegalArgumentException("Expert not found with ID: " + expertId));

        // 2. Expert -> ExpertDetailResponseDto로 변환
        return mapToDetailResponseDto(expert);
    }

    /**
     * Expert 엔티티를 ExpertResponseDto로 변환합니다.
     */
    private ExpertResponseDto mapToResponseDto(Expert expert) {
        return ExpertResponseDto.builder()
                .name(expert.getMember().getName()) // Member 이름 (추가적으로 Member 엔티티에 getter 필요)
                .mainCategoryName(expert.getMainCategory().getName())
                .careerYears(expert.getCareerYears())
                .introduction(expert.getIntroduction())
                .build();
    }

    /**
     * Expert 엔티티 -> ExpertDetailResponseDto로 변환합니다.
     */
    private ExpertDetailResponseDto mapToDetailResponseDto(Expert expert) {
        return ExpertDetailResponseDto.builder()
                .id(expert.getId())
                .name(expert.getMember().getName()) // Member 엔티티의 name
                .mainCategoryName(expert.getMainCategory().getName())
                .subCategoryNames(expert.getSubCategories().stream()
                        .map(sc -> sc.getSubCategory().getName())
                        .collect(Collectors.toList()))
                .introduction(expert.getIntroduction())
                .careerYears(expert.getCareerYears())
                .certification(expert.getCertification())
                .gender(expert.getGender())
                .build();
    }
}
package com.ll.dopdang.domain.expert.service;

import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 전문가 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 전문가를 등록합니다.
     *
     * @param expertRequestDto 전문가 정보를 담은 DTO.
     * @param memberId 전문가가 될 회원 ID.
     * @throws IllegalArgumentException 회원 또는 카테고리가 존재하지 않을 경우 예외 발생.
     */
    public void createExpert(ExpertRequestDto expertRequestDto, Long memberId) {
        // 회원(Member) 조회 및 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 카테고리(Category) 조회 및 검증
        Category category = categoryRepository.findByName(expertRequestDto.getCategoryName())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Expert 엔티티 빌더를 활용하여 객체 생성
        Expert expert = Expert.builder()
                .member(member)
                .category(category)
                .careerYears(expertRequestDto.getCareerYears())
                .introduction(expertRequestDto.getIntroduction())
                .certification(expertRequestDto.getCertificatation())
                .bankName(expertRequestDto.getBankName())
                .gender(expertRequestDto.getGender())
                .accountNumber(expertRequestDto.getAccountNumber())
                .build();

        // Expert 엔티티 저장
        expertRepository.save(expert);
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
                .map(expert -> mapToResponseDto(expert))
                .toList();
    }

    /**
     * Expert 엔티티를 ExpertResponseDto로 변환합니다.
     *
     * @param expert 변환 대상 Expert 엔티티.
     * @return 변환된 ExpertResponseDto.
     */
    private ExpertResponseDto mapToResponseDto(Expert expert) {
        Member member = expert.getMember();
        Category category = expert.getCategory();

        return ExpertResponseDto.builder()
                .categoryName(category.getName())
                .introduction(expert.getIntroduction())
                .careerYears(expert.getCareerYears())
                .certification(expert.getCertification())
                .name(member.getName())
                .build();
    }
}

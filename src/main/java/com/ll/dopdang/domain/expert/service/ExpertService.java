package com.ll.dopdang.domain.expert.service;


import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.expert.dto.request.ExpertRequestDto;
import com.ll.dopdang.domain.expert.dto.response.ExpertResponseDto;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    public void createExpert(ExpertRequestDto expertRequestDto, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Category category = categoryRepository.findById(expertRequestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
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
        expertRepository.save(expert);
    }

    public List<ExpertResponseDto> getAllExperts() {
        return expertRepository.findAll().stream().map(expert -> {
            Member member = expert.getMember();
            Category category = expert.getCategory();
            return ExpertResponseDto.builder()
                    .categoryName(category.getName())
                    .introduction(expert.getIntroduction())
                    .careerYears(expert.getCareerYears())
                    .certification(expert.getCertification())
                    .name(member.getName())
                    .build();
        }).toList();
    }
}

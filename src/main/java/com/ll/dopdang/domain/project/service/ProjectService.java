package com.ll.dopdang.domain.project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.project.dto.ProjectCreateRequest;
import com.ll.dopdang.domain.project.entity.Project;
import com.ll.dopdang.domain.project.entity.ProjectImage;
import com.ll.dopdang.domain.project.entity.ProjectStatus;
import com.ll.dopdang.domain.project.repository.ProjectImageRepository;
import com.ll.dopdang.domain.project.repository.ProjectRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
	private final ProjectRepository projectRepository;
	private final MemberRepository memberRepository;
	private final CategoryRepository categoryRepository;
	private final ExpertRepository expertRepository;
	private final ProjectImageRepository projectImageRepository;

	public Long createProject(ProjectCreateRequest request, Long clientId) {
		// 1. 클라이언트(회원) 조회
		Member client = memberRepository.findById(clientId)
			.orElseThrow(() -> new ServiceException(ErrorCode.MEMBER_NOT_FOUND));

		// 2. 카테고리 조회
		Category category = categoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new ServiceException(ErrorCode.CATEGORY_NOT_FOUND));

		// 3. 전문가가 선택되었는지 확인 후 조회 (선택적)
		Expert expert = null;
		if (request.getExpertId() != null) {
			expert = expertRepository.findById(request.getExpertId())
				.orElseThrow(() -> new ServiceException(ErrorCode.EXPERT_NOT_FOUND));
		}

		// 4. 프로젝트 엔티티 생성
		Project project = Project.builder()
			.client(client)
			.category(category)
			.title(request.getTitle())
			.summary(request.getSummary())
			.description(request.getDescription())
			.region(request.getRegion())
			.budget(request.getBudget())
			.deadline(request.getDeadline())
			.status(ProjectStatus.OPEN)
			.expert(expert)
			.build();

		// 5. 저장
		Project savedProject = projectRepository.save(project);

		// 6. 이미지가 있다면 ProjectImage 엔티티로 변환 후 저장
		List<String> imageUrls = request.getImageUrls();
		if (imageUrls != null && !imageUrls.isEmpty()) {
			List<ProjectImage> images = new ArrayList<>();
			int order = 0;
			for (String url : imageUrls) {
				images.add(ProjectImage.builder()
					.project(savedProject)
					.imageUrl(url)
					.orderNum(order++) // 순서 보장
					.build());
			}
			projectImageRepository.saveAll(images);
		}

		// 7. 최종적으로 프로젝트 ID 반환
		return savedProject.getId();
	}
}

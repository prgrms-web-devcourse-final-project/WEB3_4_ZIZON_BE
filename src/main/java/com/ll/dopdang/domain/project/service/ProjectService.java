package com.ll.dopdang.domain.project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import com.ll.dopdang.domain.expert.entity.Expert;
import com.ll.dopdang.domain.expert.repository.ExpertRepository;
import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.repository.MemberRepository;
import com.ll.dopdang.domain.project.dto.ProjectCreateRequest;
import com.ll.dopdang.domain.project.dto.ProjectDetailResponse;
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

	/**
	 * 새로운 프로젝트를 생성하고, 프로젝트 이미지가 있다면 함께 저장합니다.
	 * 요청 객체에서 전달받은 정보로 Project 엔티티를 생성하며,
	 * 프로젝트에 연결된 카테고리와 클라이언트(Member)를 조회합니다.
	 * 선택적으로 전문가(Expert)도 연결할 수 있습니다.
	 * 프로젝트 생성 후 이미지 URL이 존재할 경우, ProjectImage 엔티티를 순서대로 저장합니다.
	 *
	 * @param request   프로젝트 생성 요청 DTO (카테고리 ID, 제목, 설명, 지역, 예산, 마감일, 이미지 URL 등 포함)
	 * @param clientId  현재 인증된 사용자(클라이언트)의 ID
	 * @return 생성된 프로젝트의 ID
	 */
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

	/**
	 * 프로젝트 ID로 단건 조회
	 *
	 * @param projectId 조회할 프로젝트의 ID
	 * @return 프로젝트 상세 응답 DTO
	 * @throws ServiceException 프로젝트가 존재하지 않을 경우 예외 발생
	 */
	@Transactional(readOnly = true)
	public ProjectDetailResponse getProjectById(Long projectId) {
		// 1. 프로젝트 조회
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new ServiceException(ErrorCode.PROJECT_NOT_FOUND));

		// 2. 프로젝트에 연결된 이미지들을 순서대로 조회
		List<ProjectImage> images = projectImageRepository.findByProjectIdOrderByOrderNumAsc(projectId);

		// 3. 이미지 URL만 추출
		List<String> imageUrls = images.stream()
			.map(ProjectImage::getImageUrl)
			.toList();

		// 4. DTO 변환 및 반환
		return ProjectDetailResponse.of(project, imageUrls);
	}

}

package com.ll.dopdang.domain.category.service;

import com.ll.dopdang.domain.category.dto.request.CategoryRequestDto;
import com.ll.dopdang.domain.category.dto.response.CategoryResponseDto;
import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository; // 카테고리 데이터를 관리하는 Repository

    /**
     * 새로운 카테고리를 생성합니다.
     *
     * @param categoryRequestDto 카테고리 생성 정보를 담고 있는 DTO
     * @throws IllegalArgumentException 부모 카테고리가 없거나 타입 불일치 시 예외 발생
     */
    public void createCategory(CategoryRequestDto categoryRequestDto) {
        int level = categoryRequestDto.getLevel(); // 카테고리 레벨 (1, 2, ...)
        Integer parentId = categoryRequestDto.getParentId(); // 부모 카테고리 ID
        Category parent = null;

        // 1레벨 이상의 카테고리 생성 시 부모 카테고리 검증
        if (level > 1 && parentId != null) {
            parent = categoryRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("부모 카테고리가 존재하지 않습니다."));

            // 부모 카테고리와 자식 카테고리 타입 검증
            if (!parent.getCategoryType().equals(categoryRequestDto.getCategoryType())) {
                throw new IllegalArgumentException(String.format("부모와 자식의 categoryType이 일치해야 합니다. 부모 categoryType: %s, 자식 categoryType: %s", parent.getCategoryType(), categoryRequestDto.getCategoryType()));
            }
        }

        // 새 카테고리 ID 생성
        Integer newId = generateCategoryId(level, parentId);

        // 카테고리 빌더로 객체 생성
        Category category = Category.builder().id(newId).parent(parent).name(categoryRequestDto.getName()).level(level).categoryType(categoryRequestDto.getCategoryType()).build();

        // 카테고리 저장
        categoryRepository.save(category);
    }

    /**
     * 모든 카테고리 데이터를 조회합니다.
     *
     * @return 모든 카테고리 데이터를 DTO 리스트로 반환
     */
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll(); // 모든 카테고리를 조회
        List<CategoryResponseDto> response = new ArrayList<>();

        // 엔티티를 Response DTO로 변환
        for (Category category : categories) {
            response.add(new CategoryResponseDto(category));
        }

        return response;
    }

    /**
     * 카테고리 ID를 생성합니다.
     *
     * @param level    카테고리 레벨
     * @param parentId 부모 카테고리 ID
     * @return 새로 생성된 카테고리 ID
     */
    private Integer generateCategoryId(int level, Integer parentId) {
        if (level == 1) {
            // 1레벨 카테고리 ID: 1000 단위로 생성
            return categoryRepository.findTopByLevelOrderByIdDesc(level).map(category -> category.getId() + 1000).orElse(1000); // 첫 1레벨 카테고리는 1000
        } else {
            // 2레벨 이상 카테고리 ID: 부모 ID 기반으로 마지막 자식 + 1
            return categoryRepository.findTopByParentIdOrderByIdDesc(parentId).map(category -> category.getId() + 1).orElse(parentId + 1); // 자식 데이터가 없을 경우: 부모 ID * 100 + 1
        }
    }
}

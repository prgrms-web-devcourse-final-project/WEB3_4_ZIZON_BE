package com.ll.dopdang.domain.category.service;

import com.ll.dopdang.domain.category.dto.request.CategoryRequestDto;
import com.ll.dopdang.domain.category.dto.response.CategoryResponseDto;
import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public void createCategory(CategoryRequestDto categoryRequestDto) {

        int level = categoryRequestDto.getLevel();
        Integer parentId = categoryRequestDto.getParentId();
        Category parent = null;

        if (level > 1 && parentId != null) {
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 카테고리가 존재하지 않습니다."));
            if (!parent.getCategoryType().equals(categoryRequestDto.getCategoryType())) {
                        throw new IllegalArgumentException("부모와 자식의 categoryType이 일치해야 합니다. " +
                                "부모 categoryType: " + parent.getCategoryType() +
                                ", 자식 categoryType: " + categoryRequestDto.getCategoryType());
            }
        }
        Integer newId = generateCategoryId(level, parentId);
        Category category = Category.builder()
                .id(newId)
                .parent(parent)
                .name(categoryRequestDto.getName())
                .level(level)
                .categoryType(categoryRequestDto.getCategoryType())
                .build();

        categoryRepository.save(category);
    }

    public List<CategoryResponseDto> getAllCategories(){
        List<Category> categories = categoryRepository.findAll();
        List<CategoryResponseDto> response = new ArrayList<>();
        for(Category category : categories) {
            response.add(new CategoryResponseDto(category));
        }
        return response;
    }

    private Integer generateCategoryId(int level, Integer parentId) {
        if (level == 1) { // 1레벨: 1000 단위로 ID 생성
            return categoryRepository.findTopByLevelOrderByIdDesc(level)
                    .map(category -> category.getId() + 1000)
                    .orElse(1000); // 첫번째 대분류 ID = 1000
        } else { // 2레벨 이상: parentId 기반으로 마지막 자식 + 1 ID
            return categoryRepository.findTopByParentIdOrderByIdDesc(parentId)
                    .map(category -> category.getId() + 1)
                    .orElse(parentId + 1); // 자식이 없을 경우, 부모 ID * 100 + 1
        }
    }
}
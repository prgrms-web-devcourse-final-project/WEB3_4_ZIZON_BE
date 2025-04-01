package com.ll.dopdang.domain.category.service;

import com.ll.dopdang.domain.category.dto.request.CategoryRequestDto;
import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        }

        Integer newId = generateCategoryId(level, parentId);
        Category category = new Category(
                newId,
                parent,
                null,
                categoryRequestDto.getName(),
                level,
                categoryRequestDto.getCategoryType(),
                null
        );
    }

    private Integer generateCategoryId(int level, Integer parentId) {
        if (level == 1) {   // 1레벨: 1000 단위로 ID 생성
            return categoryRepository.findTopByLevelOrderByIdDesc(level)
                    .map(category -> category.getId() + 1000)
                    .orElse(1000); // 첫번째 대분류 ID = 1000
        } else {    // 2레벨 이상: parentId 기반으로 마지막 자식 + 1 ID
            return categoryRepository.findTopByParentIdOrderByIdDesc(parentId)
                    .map(category -> category.getId() + 1)
                    .orElse(parentId * 100 + 1); // 자식이 없을 경우, 부모 ID * 100 + 1
        }
    }



}
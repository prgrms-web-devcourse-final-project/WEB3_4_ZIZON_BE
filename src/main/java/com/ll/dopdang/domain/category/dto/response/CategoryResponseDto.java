package com.ll.dopdang.domain.category.dto.response;

import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    long id;
    private Integer parentId;
    private String name;
    private int level;
    private String categoryType;

    public CategoryResponseDto(Category category) {
        if (category.getParent() != null) {
            this.parentId = category.getParent().getId();
        } else {
            this.parentId = null;
        }

        this.id = category.getId();
        this.name = category.getName();
        this.level = category.getLevel();
        this.categoryType = category.getCategoryType().name(); // Enum -> String 변환
    }
}

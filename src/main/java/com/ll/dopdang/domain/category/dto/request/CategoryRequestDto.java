package com.ll.dopdang.domain.category.dto.request;

import com.ll.dopdang.domain.category.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDto {
    private Integer parentId;
    private String name;
    private int level;
    private CategoryType categoryType;
}

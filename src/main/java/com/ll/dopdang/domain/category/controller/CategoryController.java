package com.ll.dopdang.domain.category.controller;

import com.ll.dopdang.domain.BaseResponse;
import com.ll.dopdang.domain.category.dto.request.CategoryRequestDto;
import com.ll.dopdang.domain.category.entity.Category;
import com.ll.dopdang.domain.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<String> createCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        categoryService.createCategory(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("카테고리 생성 성공");
    }

}

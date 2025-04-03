package com.ll.dopdang.domain.category.controller;

import com.ll.dopdang.domain.category.dto.request.CategoryRequestDto;
import com.ll.dopdang.domain.category.dto.response.CategoryResponseDto;
import com.ll.dopdang.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategoryController
 *
 * 카테고리 생성/조회와 관련된 HTTP 요청을 처리하는 컨트롤러 클래스
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * POST /categories
     * 새 카테고리를 생성합니다.
     *
     * @param categoryRequestDto 클라이언트로부터 전달받은 카테고리 생성 요청 데이터
     * @return ResponseEntity<String> 성공 메시지와 HTTP 상태코드 반환
     */
    @PostMapping
    public ResponseEntity<String> createCategory(@RequestBody CategoryRequestDto categoryRequestDto) {
        categoryService.createCategory(categoryRequestDto);

        // HTTP 201 반환: 카테고리 생성 성공
        return ResponseEntity.status(HttpStatus.CREATED).body("카테고리 생성 성공");
    }

    /**
     * GET /categories
     * 전체 카테고리를 조회합니다.
     *
     * @return ResponseEntity<List<CategoryResponseDto>> 카테고리 목록과 HTTP 상태코드 반환
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories() {
        List<CategoryResponseDto> response = categoryService.getAllCategories();

        // HTTP 200 반환: 전체 카테고리 조회 성공
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
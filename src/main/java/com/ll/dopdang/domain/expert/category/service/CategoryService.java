package com.ll.dopdang.domain.expert.category.service;

import org.springframework.stereotype.Service;

import com.ll.dopdang.domain.expert.category.entity.Category;
import com.ll.dopdang.domain.expert.category.repository.CategoryRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;

	public Category findById(Long id) {
		return categoryRepository.findById(id).orElseThrow(() ->
			new ServiceException(ErrorCode.CATEGORY_NOT_FOUND)
		);
	}
}

package com.ll.dopdang.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalContentUpdateRequest {
	private Long id;
	private String fileName;
	private String fileUrl;
	private Long fileSize;
	private String fileType;
	private Integer downloadLimit;
}

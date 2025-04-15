package com.ll.dopdang.domain.store.dto;

public record DigitalContentUpdateRequest(
	Long id,
	String fileName,
	String fileUrl,
	Long fileSize,
	String fileType,
	Integer downloadLimit
) {
}

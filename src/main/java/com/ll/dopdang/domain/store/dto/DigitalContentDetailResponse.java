package com.ll.dopdang.domain.store.dto;

import com.ll.dopdang.domain.store.entity.DigitalContent;

public record DigitalContentDetailResponse(
	Long id,
	String fileName,
	String fileUrl,
	Long fileSize,
	String fileType,
	Integer downloadLimit
) {
	public static DigitalContentDetailResponse of(DigitalContent digitalContent) {
		return new DigitalContentDetailResponse(
			digitalContent.getId(),
			digitalContent.getFileName(),
			digitalContent.getFileUrl(),
			digitalContent.getFileSize(),
			digitalContent.getFileType(),
			digitalContent.getDownloadLimit()
		);
	}

	public static DigitalContentDetailResponse from(DigitalContent digitalContent) {
		return new DigitalContentDetailResponse(
			digitalContent.getId(),
			digitalContent.getFileName(),
			digitalContent.getFileUrl(),
			digitalContent.getFileSize(),
			digitalContent.getFileType(),
			digitalContent.getDownloadLimit()
		);
	}
}

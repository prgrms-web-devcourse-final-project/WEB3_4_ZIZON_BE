package com.ll.dopdang.domain.store.dto;

import com.ll.dopdang.domain.store.entity.DigitalContent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DigitalContentDetailResponse {
	private Long id;
	private String fileName;
	private String fileUrl;
	private Long fileSize;
	private String fileType;
	private Integer downloadLimit;

	public static DigitalContentDetailResponse of(DigitalContent digitalContent) {
		return DigitalContentDetailResponse.builder()
			.id(digitalContent.getId())
			.fileName(digitalContent.getFileName())
			.fileUrl(digitalContent.getFileUrl())
			.fileSize(digitalContent.getFileSize())
			.fileType(digitalContent.getFileType())
			.downloadLimit(digitalContent.getDownloadLimit())
			.build();
	}

	public static DigitalContentDetailResponse from(DigitalContent digitalContent) {
		return DigitalContentDetailResponse.builder()
			.id(digitalContent.getId())
			.fileName(digitalContent.getFileName())
			.fileUrl(digitalContent.getFileUrl())
			.fileSize(digitalContent.getFileSize())
			.fileType(digitalContent.getFileType())
			.downloadLimit(digitalContent.getDownloadLimit())
			.build();
	}
}

package com.ll.dopdang.domain.store.entity;

import com.ll.dopdang.domain.store.dto.DigitalContentProjection;
import com.ll.dopdang.domain.store.dto.DigitalContentRequest;
import com.ll.dopdang.domain.store.dto.DigitalContentUpdateRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "digital_content")
public class DigitalContent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	@NotNull
	@Column(name = "file_name")
	private String fileName;

	@NotNull
	@Column(name = "file_url")
	private String fileUrl;

	@NotNull
	@Column(name = "file_size")
	private Long fileSize;

	@NotNull
	@Column(name = "file_type")
	private String fileType;

	@NotNull
	@Column(name = "download_limit")
	private Integer downloadLimit;

	public static DigitalContent from(DigitalContentRequest request, Product product) {
		return DigitalContent.builder()
			.product(product)
			.fileName(request.fileName())
			.fileUrl(request.fileUrl())
			.fileSize(request.fileSize())
			.fileType(request.fileType())
			.downloadLimit(request.downloadLimit())
			.build();
	}

	public static DigitalContent from(DigitalContentProjection projection) {
		return DigitalContent.builder()
			.id(projection.getId())
			.fileName(projection.getFileName())
			.fileUrl(projection.getFileUrl())
			.fileSize(projection.getFileSize())
			.fileType(projection.getFileType())
			.downloadLimit(projection.getDownloadLimit())
			.build();
	}

	public static DigitalContent update(DigitalContentUpdateRequest request, Product product) {
		return DigitalContent.builder()
			.id(request.id())
			.product(product)
			.fileName(request.fileName())
			.fileUrl(request.fileUrl())
			.fileSize(request.fileSize())
			.fileType(request.fileType())
			.downloadLimit(request.downloadLimit())
			.build();
	}
}

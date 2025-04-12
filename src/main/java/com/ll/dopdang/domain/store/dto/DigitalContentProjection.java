package com.ll.dopdang.domain.store.dto;

/**
 * Projection interface for DigitalContent entity
 * Used for native SQL queries to avoid entity conversion issues
 */
public interface DigitalContentProjection {
	Long getId();

	String getFileName();

	String getFileUrl();

	Long getFileSize();

	String getFileType();

	Integer getDownloadLimit();
}

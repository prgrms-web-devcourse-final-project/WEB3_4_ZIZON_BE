package com.ll.dopdang.domain.post.dto.response;

import java.time.LocalDateTime;

import com.ll.dopdang.domain.post.entity.Comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentDetailResponse {
	private Long id;
	private String authorName;
	private String authorImage;
	private String content;
	private LocalDateTime createdAt;

	public static CommentDetailResponse of(Comment comment) {
		return CommentDetailResponse.builder()
			.id(comment.getId())
			.authorName(comment.getAdmin().getName())
			.authorImage(comment.getAdmin().getProfileImage())
			.content(comment.getContent())
			.createdAt(comment.getCreatedAt())
			.build();
	}
}

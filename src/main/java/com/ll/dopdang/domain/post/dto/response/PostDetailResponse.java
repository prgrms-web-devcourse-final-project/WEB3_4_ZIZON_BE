package com.ll.dopdang.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ll.dopdang.domain.post.entity.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostDetailResponse {
	private Long id;
	private String authorName;
	private String authorImage;
	private String title;
	private String content;
	private Integer viewCount;
	private LocalDateTime createdAt;
	private Boolean isAnswered;
	private List<CommentDetailResponse> comments;

	public static PostDetailResponse of(Post post) {
		List<CommentDetailResponse> commentResponses = new ArrayList<>();

		if (post.getComments() != null) {
			commentResponses = post.getComments().stream()
				.map(CommentDetailResponse::of)
				.collect(Collectors.toList());
		}

		return PostDetailResponse.builder()
			.id(post.getId())
			.authorName(post.getMember().getName())
			.authorImage(post.getMember().getProfileImage())
			.title(post.getTitle())
			.content(post.getContent())
			.viewCount(post.getViewCount())
			.createdAt(post.getCreatedAt())
			.isAnswered(post.getIsAnswered())
			.comments(commentResponses)
			.build();
	}
}

package com.ll.dopdang.domain.post.dto.response;

import java.time.LocalDateTime;

import com.ll.dopdang.domain.post.entity.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostAllResponse {
	private Long id;
	private String authorName;
	private String authorImage;
	private String title;
	private Integer viewCount;
	private LocalDateTime createdAt;
	private Boolean isAnswered;

	public static PostAllResponse of(Post post) {
		return PostAllResponse.builder()
			.id(post.getId())
			.authorName(post.getMember().getName())
			.authorImage(post.getMember().getProfileImage())
			.title(post.getTitle())
			.viewCount(post.getViewCount())
			.createdAt(post.getCreatedAt())
			.isAnswered(post.getIsAnswered())
			.build();
	}
}

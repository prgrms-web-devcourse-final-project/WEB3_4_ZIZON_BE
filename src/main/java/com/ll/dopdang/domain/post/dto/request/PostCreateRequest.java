package com.ll.dopdang.domain.post.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class PostCreateRequest {
	@NonNull
	private String title;
	@NonNull
	private String content;
}

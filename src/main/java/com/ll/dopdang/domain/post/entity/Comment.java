package com.ll.dopdang.domain.post.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CurrentTimestamp;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.post.dto.request.CommentCreateRequest;
import com.ll.dopdang.domain.post.dto.request.CommentUpdateRequest;

import io.jsonwebtoken.lang.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id")
	private Member admin;

	private String content;

	@CurrentTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * Comment 생성 메서드
	 * @param request request 댓글 생성 dto
	 * @param post post
	 * @param admin admin
	 * @return {@link Comment}
	 */
	public static Comment from(CommentCreateRequest request, Post post, Member admin) {
		return Comment.builder()
			.post(post)
			.admin(admin)
			.content(request.getContent())
			.build();
	}

	/**
	 * Comment 수정 메서드
	 * @param request request 댓글 수정 dto
	 * @param post post
	 * @param comment comment
	 * @return {@link Comment}
	 */
	public static Comment update(CommentUpdateRequest request, Post post, Comment comment) {
		return Comment.builder()
			.id(comment.getId())
			.post(post)
			.admin(comment.getAdmin())
			.content(!Objects.isEmpty(request.getContent()) ? request.getContent() : comment.getContent())
			.createdAt(comment.getCreatedAt())
			.build();
	}
}

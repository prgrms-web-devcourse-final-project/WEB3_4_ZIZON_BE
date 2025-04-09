package com.ll.dopdang.domain.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CurrentTimestamp;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.post.dto.request.PostCreateRequest;
import com.ll.dopdang.domain.post.dto.request.PostUpdateRequest;

import io.jsonwebtoken.lang.Objects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@NonNull
	private String title;

	@NonNull
	private String content;

	@Column(name = "view_count")
	private Integer viewCount;

	@CurrentTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "is_answered")
	private Boolean isAnswered;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	private List<Comment> comments = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.viewCount = 0;
		this.isAnswered = false;
	}

	/**
	 * 문의글 생성 메서드
	 * @param request 문의글 생성 dto
	 * @param author 문의글 작성자
	 * @return {@link Post}
	 */
	public static Post from(PostCreateRequest request, Member author) {
		return Post.builder()
			.member(author)
			.title(request.getTitle())
			.content(request.getContent())
			.build();
	}

	/**
	 * 문의글 조회 + 조회수 증가 메서드
	 * @param post post 문의글
	 * @return {@link Post}
	 */
	public static Post incrementViewCount(Post post) {
		return Post.builder()
			.id(post.getId())
			.member(post.getMember())
			.title(post.getTitle())
			.content(post.getContent())
			.viewCount(post.getViewCount() + 1)
			.createdAt(post.getCreatedAt())
			.isAnswered(post.getIsAnswered())
			.comments(post.getComments())
			.build();
	}

	/**
	 * 문의글 수정 메서드
	 * @param request request 문의글 수정 dto
	 * @param post post 문의글
	 * @return {@link Post}
	 */
	public static Post update(PostUpdateRequest request, Post post) {
		return Post.builder()
			.id(post.getId())
			.member(post.getMember())
			.title(!Objects.isEmpty(request.getTitle()) ? request.getTitle() : post.getTitle())
			.content(!Objects.isEmpty(request.getContent()) ? request.getContent() : post.getContent())
			.viewCount(post.getViewCount())
			.createdAt(post.getCreatedAt())
			.isAnswered(post.getIsAnswered())
			.comments(post.getComments())
			.build();
	}

	/**
	 * 댓글이 추가된 문의글 메서드
	 * @param post post 문의글
	 * @return {@link Post}
	 */
	public static Post addComment(Post post) {
		return Post.builder()
			.id(post.getId())
			.member(post.getMember())
			.title(post.getTitle())
			.content(post.getContent())
			.viewCount(post.getViewCount())
			.createdAt(post.getCreatedAt())
			.isAnswered(true)
			.comments(post.getComments())
			.build();
	}

	/**
	 * 댓글이 없을 경우, isAnswered의 상태를 업데이트 하는 메서드
	 * @param post post 문의글
	 * @return {@link Post}
	 */
	public static Post deleteComment(Post post) {
		return Post.builder()
			.id(post.getId())
			.member(post.getMember())
			.title(post.getTitle())
			.content(post.getContent())
			.viewCount(post.getViewCount())
			.createdAt(post.getCreatedAt())
			.isAnswered(false)
			.comments(post.getComments())
			.build();
	}
}

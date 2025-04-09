package com.ll.dopdang.domain.post.controller;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.dopdang.domain.post.dto.request.CommentCreateRequest;
import com.ll.dopdang.domain.post.dto.request.CommentUpdateRequest;
import com.ll.dopdang.domain.post.dto.request.PostCreateRequest;
import com.ll.dopdang.domain.post.dto.request.PostUpdateRequest;
import com.ll.dopdang.domain.post.service.CommentService;
import com.ll.dopdang.domain.post.service.PostService;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
	private final PostService postService;
	private final CommentService commentService;

	/**
	 * 문의글 작성 API
	 * @param userDetails 인증된 유저 정보
	 * @param request 문의글 생성 dto
	 * @return {@link ResponseEntity}
	 */
	@PostMapping
	public ResponseEntity<?> createPost(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody PostCreateRequest request) {
		postService.createPost(userDetails, request);
		return ResponseEntity.ok().body(Map.of("message", "문의글이 성공적으로 등록되었습니다."));
	}

	/**
	 * 문의글 전체 조회 API
	 * @param pageable 페이지 정보
	 * @return {@link ResponseEntity}
	 */
	@GetMapping
	public ResponseEntity<?> getPosts(
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(postService.getPosts(pageable));
	}

	/**
	 * 문의글 단건 조회 API
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @return {@link ResponseEntity}
	 */
	@GetMapping("/{post_id}")
	public ResponseEntity<?> getPostById(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("post_id") Long postId) {
		return ResponseEntity.ok(postService.getPostById(postId));
	}

	/**
	 * 문의글 수정 API
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @param request 문의글 수정 dto
	 * @return {@link ResponseEntity}
	 */
	@PatchMapping("/{post_id}")
	public ResponseEntity<?> updatePost(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("post_id") Long postId,
		@Valid @RequestBody PostUpdateRequest request) {
		postService.updatePost(userDetails, postId, request);
		return ResponseEntity.ok().body(Map.of("message", "문의글이 성공적으로 수정되었습니다."));
	}

	/**
	 * 문의글 삭제 API
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @return {@link ResponseEntity}
	 */
	@DeleteMapping("/{post_id}")
	public ResponseEntity<?> deletePost(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("post_id") Long postId) {
		postService.deletePost(userDetails, postId);
		return ResponseEntity.ok().body(Map.of("message", "문의글이 성공적으로 삭제되었습니다."));
	}

	/**
	 * 댓글 작성 API
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @param request request 댓글 생성 dto
	 * @return {@link ResponseEntity}
	 */
	@PostMapping("/{post_id}/comments")
	public ResponseEntity<?> createComment(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("post_id") Long postId,
		@Valid @RequestBody CommentCreateRequest request) {
		commentService.createComments(userDetails, postId, request);
		return ResponseEntity.ok().body(Map.of("message", "댓글이 성공적으로 등록되었습니다."));
	}

	/**
	 * 댓글 수정 API
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @param commentId comment 고유 ID
	 * @param request request 댓글 수정 dto
	 * @return {@link ResponseEntity}
	 */
	@PatchMapping("/{post_id}/comments/{comment_id}")
	public ResponseEntity<?> updateComment(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("post_id") Long postId,
		@PathVariable("comment_id") Long commentId,
		@Valid @RequestBody CommentUpdateRequest request) {
		commentService.updateComments(userDetails, postId, commentId, request);
		return ResponseEntity.ok().body(Map.of("message", "댓글이 성공적으로 수정되었습니다."));
	}

	@DeleteMapping("/{post_id}/comments/{comment_id}")
	public ResponseEntity<?> deleteComment(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable("post_id") Long postId,
		@PathVariable("comment_id") Long commentId) {
		commentService.deleteComments(userDetails, postId, commentId);
		return ResponseEntity.ok().body(Map.of("message", "댓글이 성공적으로 삭제되었습니다."));
	}
}

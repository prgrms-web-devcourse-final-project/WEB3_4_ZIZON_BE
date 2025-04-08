package com.ll.dopdang.domain.post.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.entity.MemberRole;
import com.ll.dopdang.domain.member.service.MemberUtilService;
import com.ll.dopdang.domain.post.dto.request.CommentCreateRequest;
import com.ll.dopdang.domain.post.dto.request.CommentUpdateRequest;
import com.ll.dopdang.domain.post.entity.Comment;
import com.ll.dopdang.domain.post.entity.Post;
import com.ll.dopdang.domain.post.repository.CommentRepository;
import com.ll.dopdang.domain.post.repository.PostRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final MemberUtilService memberUtilService;
	private final PostService postService;

	/**
	 * 댓글 작성 메서드
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @param request request 댓글 생성 dto
	 */
	@Transactional
	public void createComments(CustomUserDetails userDetails, Long postId, CommentCreateRequest request) {
		Member member = memberUtilService.findMember(userDetails.getId());
		isAdmin(member);
		Post post = postService.findById(postId);
		Comment comment = Comment.from(request, post, member);
		commentRepository.save(comment);

		Post updatePost = Post.addComment(post);
		postRepository.save(updatePost);
	}

	/**
	 * 댓글 수정 메서드
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @param coomentId comment 고유 ID
	 * @param request request 댓글 수정 dto
	 */
	@Transactional
	public void updateComments(CustomUserDetails userDetails, Long postId, Long coomentId,
		CommentUpdateRequest request) {
		Member member = memberUtilService.findMember(userDetails.getId());
		isAdmin(member);

		Post post = postService.findById(postId);
		Comment comment = findById(coomentId);
		isValidPost(comment, postId);

		Comment updatedComment = Comment.update(request, post, comment);
		commentRepository.save(updatedComment);
	}

	@Transactional
	public void deleteComments(CustomUserDetails userDetails, Long postId, Long commentId) {
		Member member = memberUtilService.findMember(userDetails.getId());
		isAdmin(member);

		Comment comment = findById(commentId);
		isValidPost(comment, postId);
		commentRepository.delete(comment);

		Post post = postService.findById(postId);
		Long commentCount = commentRepository.countByPostId(postId);
		if (commentCount == 0) {
			Post updatePost = Post.deleteComment(post);
			postRepository.save(updatePost);
		}
	}

	/**
	 * 댓글 작성자가 운영자인지 검증하는 메서드 (MemberUtilService로 옮기는 게 좋을 듯)
	 * @param member 유저
	 */
	public void isAdmin(Member member) {
		if (!Objects.equals(member.getUserRole(), MemberRole.ADMIN.toString())) {
			throw new ServiceException(ErrorCode.INVALID_COMMENT_AUTHOR);
		}
	}

	/**
	 * 댓글의 문의글 ID와 postId를 비교하는 메서드
	 * @param comment comment
	 * @param postId post 고유 ID
	 */
	public void isValidPost(Comment comment, Long postId) {
		if (!Objects.equals(comment.getPost().getId(), postId)) {
			throw new ServiceException(ErrorCode.DISMATCH_COMMENT_AND_POST);
		}
	}

	/**
	 * Id값으로 Comment를 가져오는 메서드
	 * @param id comment 고유 ID
	 * @return {@link Comment}
	 */
	@Transactional(readOnly = true)
	public Comment findById(Long id) {
		return commentRepository.findById(id).orElseThrow(() -> new ServiceException(ErrorCode.COMMENT_NOT_FOUND));
	}
}

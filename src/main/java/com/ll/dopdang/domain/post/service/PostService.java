package com.ll.dopdang.domain.post.service;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ll.dopdang.domain.member.entity.Member;
import com.ll.dopdang.domain.member.service.MemberUtilService;
import com.ll.dopdang.domain.post.dto.request.PostCreateRequest;
import com.ll.dopdang.domain.post.dto.request.PostUpdateRequest;
import com.ll.dopdang.domain.post.dto.response.PostAllResponse;
import com.ll.dopdang.domain.post.dto.response.PostDetailResponse;
import com.ll.dopdang.domain.post.entity.Post;
import com.ll.dopdang.domain.post.repository.PostRepository;
import com.ll.dopdang.global.exception.ErrorCode;
import com.ll.dopdang.global.exception.ServiceException;
import com.ll.dopdang.global.security.custom.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
	private final MemberUtilService memberUtilService;

	/**
	 * 문의글 작성 메서드
	 * @param userDetails 인증된 유저 정보
	 * @param request 문의글 생성 dto
	 */
	@Transactional
	public void createPost(CustomUserDetails userDetails, PostCreateRequest request) {
		Member member = memberUtilService.findMember(userDetails.getId());
		Post post = Post.from(request, member);
		postRepository.save(post);
	}

	/**
	 * 문의글 전체 조회 메서드
	 * @param pageable 페이지 정보
	 * @return {@link Page<PostAllResponse>}
	 */
	public Page<PostAllResponse> getPosts(Pageable pageable) {
		Page<Post> posts = postRepository.findAll(pageable);
		return posts.map(PostAllResponse::of);
	}

	/**
	 * 문의글 단건 조회 메서드
	 * @param postId post 고유 ID
	 * @return {@link PostDetailResponse}
	 */
	@Transactional
	public PostDetailResponse getPostById(Long postId) {
		Post post = findByIdWithComments(postId);
		Post updatePost = Post.incrementViewCount(post);
		postRepository.save(updatePost);
		return PostDetailResponse.of(updatePost);
	}

	/**
	 * 문의글 수정 메서드
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 * @param request 문의글 수정 dto
	 */
	@Transactional
	public void updatePost(CustomUserDetails userDetails, Long postId, PostUpdateRequest request) {
		Post post = findById(postId);
		Member member = memberUtilService.findMember(userDetails.getId());
		if (!Objects.equals(member, post.getMember())) {
			throw new ServiceException(ErrorCode.INVALID_POST_AUTHOR);
		}
		postRepository.save(Post.update(request, post));
	}

	/**
	 * 문의글 삭제 메서드
	 * @param userDetails 인증된 유저 정보
	 * @param postId post 고유 ID
	 */
	@Transactional
	public void deletePost(CustomUserDetails userDetails, Long postId) {
		Post post = findById(postId);
		Member member = memberUtilService.findMember(userDetails.getId());
		if (!Objects.equals(member, post.getMember())) {
			throw new ServiceException(ErrorCode.INVALID_POST_AUTHOR);
		}
		postRepository.delete(post);
	}

	/**
	 * Id값으로 Post를 가져오는 메서드
	 * @param id post 고유 ID
	 * @return {@link Post}
	 */
	@Transactional(readOnly = true)
	public Post findById(Long id) {
		return postRepository.findById(id).orElseThrow(() -> new ServiceException(ErrorCode.POST_NOT_FOUND));
	}

	/**
	 * Id값으로 Post와 Comments를 가져오는 메서드
	 * @param id
	 * @return
	 */
	@Transactional(readOnly = true)
	public Post findByIdWithComments(Long id) {
		return postRepository.findByIdWithComments(id)
			.orElseThrow(() -> new ServiceException(ErrorCode.POST_NOT_FOUND));
	}
}

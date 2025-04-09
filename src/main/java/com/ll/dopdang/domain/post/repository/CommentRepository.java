package com.ll.dopdang.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ll.dopdang.domain.post.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	@Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
	Long countByPostId(@Param("postId") Long postId);
}

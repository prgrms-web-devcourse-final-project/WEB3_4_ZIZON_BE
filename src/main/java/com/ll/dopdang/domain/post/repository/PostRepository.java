package com.ll.dopdang.domain.post.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ll.dopdang.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	@Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
	Optional<Post> findByIdWithComments(@Param("id") Long id);

	@Override
	Page<Post> findAll(Pageable pageable);
}

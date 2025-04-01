package com.ll.dopdang.domain.category.repository;

import com.ll.dopdang.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findTopByLevelOrderByIdDesc(int level);

    Optional<Category> findTopByParentIdOrderByIdDesc(Integer parentId);

}

package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameAndUserId(String name, Long userId);
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
}

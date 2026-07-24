package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByNameAndUserId(String name, Long userId);
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
    List<Brand> findByUserId(Long userId);
    Optional<Brand> findByIdAndUserId(Long id, Long userId);
}

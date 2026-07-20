package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByBrandId(Long brandId);
    boolean existsByBrandIdAndUserId(Long brandId, Long userId);
}

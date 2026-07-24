package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
    List<Product> findByUserIdAndDeletedAtIsNullOrderByNameAsc(Long userId);
    boolean existsByBrandId(Long brandId);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByBrandIdAndBrandCode(Long brandId, String brandCode);
    boolean existsByBrandIdAndBrandCodeAndDeletedAtIsNull(Long brandId, String brandCode);
    boolean existsByBrandIdAndBrandCodeAndIdNotAndDeletedAtIsNull(Long brandId, String brandCode, Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.brand LEFT JOIN FETCH p.category WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.name ASC")
    List<Product> findAllWithBrandAndCategoryByUserId(@Param("userId") Long userId);
}

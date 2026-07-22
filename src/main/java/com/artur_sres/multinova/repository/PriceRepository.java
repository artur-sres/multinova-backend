package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long>  {
    List<Price> findByProductIdOrderByCreatedAtDesc(Long productId);
    Optional<Price> findFirstByProductIdOrderByCreatedAtDesc(Long productId);
}

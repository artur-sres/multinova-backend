package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Sale;
import com.artur_sres.multinova.domain.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByIdAndUserIdAndStatus(Long id, Long UserId, SaleStatus status);
}

package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
}

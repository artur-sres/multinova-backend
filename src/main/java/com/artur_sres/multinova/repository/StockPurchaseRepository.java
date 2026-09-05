package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.StockPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPurchaseRepository extends JpaRepository<StockPurchase, Long> {
    List<StockPurchase> findByUserId(Long userId);
}

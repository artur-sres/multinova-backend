package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.StockPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

public interface StockPurchaseRepository extends JpaRepository<StockPurchase, Long> {
    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM StockPurchase p "
            + "WHERE p.user.id = :userId AND p.entryType = com.artur_sres.multinova.domain.enums.StockEntryType.PAID "
            + "AND p.purchaseDate BETWEEN :from AND :to")
    BigDecimal sumByUserAndPeriod(@Param("userId") Long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<StockPurchase> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM StockPurchase p "
            + "WHERE p.user.id = :userId AND p.entryType IN (com.artur_sres.multinova.domain.enums.StockEntryType.PAID, "
            + "com.artur_sres.multinova.domain.enums.StockEntryType.PENDING) AND p.purchaseDate BETWEEN :from AND :to")
    BigDecimal sumPurchasesByUserAndPeriod(@Param("userId") Long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

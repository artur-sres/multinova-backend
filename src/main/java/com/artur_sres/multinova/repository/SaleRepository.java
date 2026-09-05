package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Sale;
import com.artur_sres.multinova.domain.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s "
            + "WHERE s.user.id = :userId AND s.status = com.artur_sres.multinova.domain.enums.SaleStatus.COMPLETED AND s.saleDate BETWEEN :from AND :to")
    BigDecimal sumCompletedByUserAndPeriod(@Param("userId") Long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    Optional<Sale> findByIdAndUserIdAndStatus(Long id, Long UserId, SaleStatus status);
    List<Sale> findByUserIdAndStatus(Long userId, SaleStatus status);
    List<Sale> findByUserId(Long userId);
}

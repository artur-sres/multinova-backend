package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Bill;
import com.artur_sres.multinova.domain.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b "
            + "WHERE b.user.id = :userId AND b.status = com.artur_sres.multinova.domain.enums.BillStatus.PAID AND b.paidAt BETWEEN :from AND :to")
    BigDecimal sumPaidByUserAndPeriod(@Param("userId") Long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b "
            + "WHERE b.user.id = :userId AND b.status = com.artur_sres.multinova.domain.enums.BillStatus.PENDING")
    BigDecimal sumPendingByUser(@Param("userId") Long userId);

    Optional<Bill> findByIdAndUserId(Long id, Long userId);
    List<Bill> findByUserId(Long userId);
    List<Bill> findByUserIdAndStatus(Long userId, BillStatus status);
}

package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Bill;
import com.artur_sres.multinova.domain.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByIdAndUserId(Long id, Long userId);
    List<Bill> findByUserId(Long userId);
    List<Bill> findByUserIdAndStatus(Long userId, BillStatus status);
}

package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM CustomerPayment p "
            + "WHERE p.customer.user.id = :userId AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumByUserAndPeriod(@Param("userId") Long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<CustomerPayment> findByCustomerIdOrderByPaidAtDescIdDesc(Long customerId);
}

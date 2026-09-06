package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    @Query(value = "SELECT (SELECT COALESCE(SUM(p.amount), 0) FROM customer_payments p JOIN customers c ON c.id = p.customer_id "
            + "WHERE c.user_id = :userId AND p.paid_at BETWEEN :from AND :to) - "
            + "(SELECT COALESCE(SUM(r.delta), 0) FROM customer_ledger r JOIN customer_ledger original ON original.id = r.reversed_entry_id "
            + "JOIN customers c ON c.id = r.customer_id WHERE c.user_id = :userId AND original.type = 'PAYMENT' "
            + "AND r.recorded_at BETWEEN :from AND :to)", nativeQuery = true)
    BigDecimal sumByUserAndPeriod(@Param("userId") Long userId,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<CustomerPayment> findByCustomerIdOrderByPaidAtDescIdDesc(Long customerId);
}

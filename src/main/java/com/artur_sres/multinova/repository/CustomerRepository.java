package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT COALESCE(SUM(c.debtBalance), 0) FROM Customer c "
            + "WHERE c.user.id = :userId")
    BigDecimal sumDebtBalanceByUser(@Param("userId") Long userId);

    List<Customer> findByUserId(Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
    Optional<Customer> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
}

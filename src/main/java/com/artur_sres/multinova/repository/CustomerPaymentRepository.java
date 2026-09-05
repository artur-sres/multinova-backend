package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByCustomerIdOrderByPaidAtDescIdDesc(Long customerId);
}

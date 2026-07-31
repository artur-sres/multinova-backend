package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByUserId(Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
    Optional<Customer> findByIdAndUserId(Long id, Long userId);
    boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);
}

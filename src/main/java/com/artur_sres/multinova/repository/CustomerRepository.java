package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}

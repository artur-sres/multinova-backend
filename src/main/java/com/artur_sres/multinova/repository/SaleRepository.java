package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}

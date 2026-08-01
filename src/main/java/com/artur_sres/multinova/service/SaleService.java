package com.artur_sres.multinova.service;

import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.repository.SaleItemRepository;
import com.artur_sres.multinova.repository.SaleRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SaleService {
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public SaleService(SaleRepository saleRepository, SaleItemRepository saleItemRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }


}

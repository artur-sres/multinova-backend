package com.artur_sres.multinova.service;

import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.repository.SaleItemRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SaleItemService {
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public SaleItemService(SaleItemRepository saleItemRepository, ProductRepository productRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }
}

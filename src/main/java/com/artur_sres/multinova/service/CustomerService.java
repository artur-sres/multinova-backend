package com.artur_sres.multinova.service;

import com.artur_sres.multinova.repository.CustomerRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public CustomerService(CustomerRepository customerRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.customerRepository = customerRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }


}

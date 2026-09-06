package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.customer.CustomerDebtRequestDTO;
import com.artur_sres.multinova.service.CustomerDebtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers/{customerId}/debts")
public class CustomerDebtController {
    private final CustomerDebtService service;
    public CustomerDebtController(CustomerDebtService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable Long customerId, @Valid @RequestBody CustomerDebtRequestDTO dto) {
        service.create(customerId, dto);
    }
}

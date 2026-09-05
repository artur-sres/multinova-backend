package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.customer_payment.CustomerPaymentRequestDTO;
import com.artur_sres.multinova.dto.customer_payment.CustomerPaymentResponseDTO;
import com.artur_sres.multinova.service.CustomerPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer-payments")
public class CustomerPaymentController {
    private final CustomerPaymentService customerPaymentService;

    public CustomerPaymentController(CustomerPaymentService customerPaymentService) {
        this.customerPaymentService = customerPaymentService;
    }

    @PostMapping
    public ResponseEntity<CustomerPaymentResponseDTO> create(@Valid @RequestBody CustomerPaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerPaymentService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CustomerPaymentResponseDTO>> listByCustomer(@RequestParam Long customerId) {
        return ResponseEntity.ok(customerPaymentService.listByCustomer(customerId));
    }
}

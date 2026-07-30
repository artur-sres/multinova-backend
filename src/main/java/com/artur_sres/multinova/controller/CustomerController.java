package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.customer.CustomerPatchDTO;
import com.artur_sres.multinova.dto.customer.CustomerRequestDTO;
import com.artur_sres.multinova.dto.customer.CustomerResponseDTO;
import com.artur_sres.multinova.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer (@Valid @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById (@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.searchById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomerById (@PathVariable Long id, @Valid @RequestBody CustomerPatchDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById (@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> listCustomers() {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.listAll());
    }

}

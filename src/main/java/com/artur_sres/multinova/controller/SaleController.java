package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.sale.SaleRequestDTO;
import com.artur_sres.multinova.dto.sale.SaleResponseDTO;
import com.artur_sres.multinova.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
public class SaleController {
    public final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponseDTO> createSale(@Valid @RequestBody SaleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> getSaleById(SaleRequestDTO dto) {
        return null;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> updateSaleById(SaleRequestDTO dto) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> deleteSaleById(SaleRequestDTO dto) {
        return null;
    }
}

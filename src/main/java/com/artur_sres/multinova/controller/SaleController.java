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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSaleById(@PathVariable Long id) {
        saleService.cancel(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseRequestDTO;
import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseResponseDTO;
import com.artur_sres.multinova.service.StockPurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock-purchases")
public class StockPurchaseController {
    private final StockPurchaseService stockPurchaseService;

    public StockPurchaseController(StockPurchaseService stockPurchaseService) {
        this.stockPurchaseService = stockPurchaseService;
    }

    @PostMapping
    public ResponseEntity<StockPurchaseResponseDTO> create(@Valid @RequestBody StockPurchaseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockPurchaseService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<StockPurchaseResponseDTO>> listAll() {
        return ResponseEntity.ok(stockPurchaseService.listAll());
    }
}

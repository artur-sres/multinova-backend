package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.price.PriceRequestDTO;
import com.artur_sres.multinova.dto.price.PriceResponseDTO;
import com.artur_sres.multinova.service.PriceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/prices")
public class PriceController {
    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @PostMapping
    public ResponseEntity<PriceResponseDTO> create(@PathVariable Long productId, @Valid @RequestBody PriceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(priceService.create(productId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PriceResponseDTO>> listByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(priceService.listByProduct(productId));
    }

    @GetMapping("/current")
    public ResponseEntity<PriceResponseDTO> getCurrentPrice(@PathVariable Long productId) {
        return ResponseEntity.ok(priceService.getCurrentPrice(productId));
    }
}

package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.product.ProductPatchDTO;
import com.artur_sres.multinova.dto.product.ProductRequestDTO;
import com.artur_sres.multinova.dto.product.ProductResponseDTO;
import com.artur_sres.multinova.service.ProductService;
import com.artur_sres.multinova.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct (@Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById (@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.searchById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProductById (@PathVariable Long id, @Valid @RequestBody ProductPatchDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById (@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.listAll());
    }
}
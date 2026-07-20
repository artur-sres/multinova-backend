package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.brand.BrandRequestDTO;
import com.artur_sres.multinova.dto.brand.BrandResponseDTO;
import com.artur_sres.multinova.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://127.0.0.1:5500/")
@RestController
@RequestMapping("/brands")
public class BrandController {
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<BrandResponseDTO> createBrand(@Valid @RequestBody BrandRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> getBrandByID(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(brandService.searchById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> updateBrandByID(@PathVariable Long id, @Valid @RequestBody BrandRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(brandService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrandByID(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<BrandResponseDTO>> listBrands() {
        return ResponseEntity.status(HttpStatus.OK).body(brandService.listAll());
    }
}

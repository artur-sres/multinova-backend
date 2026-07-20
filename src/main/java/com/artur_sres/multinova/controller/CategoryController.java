package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.brand.BrandResponseDTO;
import com.artur_sres.multinova.dto.category.CategoryRequestDTO;
import com.artur_sres.multinova.dto.category.CategoryResponseDTO;
import com.artur_sres.multinova.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://127.0.0.1:5500/")
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryByID(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.searchById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategoryByID(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryByID(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.listAll());
    }
}

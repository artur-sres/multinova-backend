package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.Category;
import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.product.ProductRequestDTO;
import com.artur_sres.multinova.dto.product.ProductResponseDTO;
import com.artur_sres.multinova.dto.product.ProductPatchDTO;
import com.artur_sres.multinova.exception.BrandCodeAlreadyExistsException;
import com.artur_sres.multinova.exception.BrandNotFoundException;
import com.artur_sres.multinova.exception.CategoryNotFoundException;
import com.artur_sres.multinova.exception.ProductNotFoundException;
import com.artur_sres.multinova.repository.BrandRepository;
import com.artur_sres.multinova.repository.CategoryRepository;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProductService(ProductRepository productRepository, BrandRepository brandRepository, CategoryRepository categoryRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();

        dto.setId(product.getId());
        dto.setUserId(product.getUser().getId());

        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        dto.setBrandCode(product.getBrandCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setStock(product.getStock());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }

    private void validateBrandCode(Brand brand, String brandCode, Long productId) {
        if (brand == null) {
            if (brandCode != null && !brandCode.isBlank()) {
                throw new IllegalArgumentException("A brand code can only be assigned when a brand is selected.");
            }
            return;
        }

        if (brandCode == null || brandCode.isBlank()) {
            return;
        }

        boolean exists;
        if (productId == null) {
            exists = productRepository.existsByBrandIdAndBrandCodeAndDeletedAtIsNull(brand.getId(), brandCode);
        } else {
            exists = productRepository.existsByBrandIdAndBrandCodeAndIdNotAndDeletedAtIsNull(brand.getId(), brandCode, productId);
        }

        if (exists) {
            throw new BrandCodeAlreadyExistsException("This brand code is already in use for the selected brand.");
        }
    }

    private Brand getAuthorizedBrand(Long brandId, User currentUser) {
        if (brandId == null) {
            return null;
        }

        return brandRepository.findByIdAndUserId(brandId, currentUser.getId())
                .orElseThrow(() -> new BrandNotFoundException("Brand not found."));
    }

    private Category getAuthorizedCategory(Long categoryId, User currentUser) {

        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndUserId(categoryId, currentUser.getId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found."));
    }

    private Product getAuthorizedProduct(Long productId, User currentUser) {
        Product product = productRepository.findByIdAndUserIdAndDeletedAtIsNull(productId, currentUser.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));

        return product;
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto) {

        User currentUser = authenticatedUserProvider.getCurrentUser();

        Brand brand = getAuthorizedBrand(dto.getBrandId(), currentUser);
        Category category = getAuthorizedCategory(dto.getCategoryId(), currentUser);

        validateBrandCode(brand, dto.getBrandCode(), null);

        Product product = new Product();
        product.setUser(currentUser);
        product.setBrand(brand);
        product.setCategory(category);
        product.setBrandCode(dto.getBrandCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        product.setStock(dto.getStock());

        Product saved = productRepository.save(product);

        return toResponseDTO(saved);
    }

    public ProductResponseDTO searchById(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Product product = getAuthorizedProduct(id, currentUser);

        return toResponseDTO(product);
    }

    public List<ProductResponseDTO> listAll() {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        List<Product> products = productRepository.findAllWithBrandAndCategoryByUserId(currentUser.getId());

        return products.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductPatchDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Product product = getAuthorizedProduct(id, currentUser);

        if (dto.getBrandId() != null) {
            Brand brand = getAuthorizedBrand(dto.getBrandId(), currentUser);
            product.setBrand(brand);
        }

        if (dto.getCategoryId() != null) {
            Category category = getAuthorizedCategory(dto.getCategoryId(), currentUser);
            product.setCategory(category);
        }

        if (dto.getBrandCode() != null) {
            validateBrandCode(product.getBrand(), dto.getBrandCode(), product.getId());
            product.setBrandCode(dto.getBrandCode());
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            product.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }

        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }

        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }


        return toResponseDTO(product);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Product product = getAuthorizedProduct(id, currentUser);

        product.setDeletedAt(LocalDateTime.now());
    }

}

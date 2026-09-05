package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.Category;
import com.artur_sres.multinova.domain.Price;
import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.product.ProductRequestDTO;
import com.artur_sres.multinova.dto.product.ProductResponseDTO;
import com.artur_sres.multinova.dto.product.ProductPatchDTO;
import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseRequestDTO;
import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemRequestDTO;
import com.artur_sres.multinova.exception.*;
import com.artur_sres.multinova.mapper.ProductMapper;
import com.artur_sres.multinova.repository.BrandRepository;
import com.artur_sres.multinova.repository.CategoryRepository;
import com.artur_sres.multinova.repository.PriceRepository;
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
    private final ProductMapper productMapper;
    private final PriceRepository priceRepository;
    private final StockPurchaseService stockPurchaseService;

    public ProductService(ProductRepository productRepository, BrandRepository brandRepository, CategoryRepository categoryRepository, AuthenticatedUserProvider authenticatedUserProvider, ProductMapper productMapper, PriceRepository priceRepository, StockPurchaseService stockPurchaseService) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.productMapper = productMapper;
        this.priceRepository = priceRepository;
        this.stockPurchaseService = stockPurchaseService;
    }

    private void validateBrandCode(Brand brand, String brandCode, Long productId) {
        if (brand == null) {
            if (brandCode != null && !brandCode.isBlank()) {
                throw new InvalidBrandCodeAssignmentException("A brand code can only be assigned when a brand is selected.");
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
        return productRepository.findByIdAndUserIdAndDeletedAtIsNull(productId, currentUser.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO response = productMapper.toResponseDTO(product);
        Price price = priceRepository.findFirstByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow(() -> new PriceNotFoundException("Price not found."));
        response.setPurchasePrice(price.getPurchasePrice());
        response.setSalePrice(price.getSalePrice());
        return response;
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto) {

        if (dto.getPrice().getSalePrice().compareTo(dto.getPrice().getPurchasePrice()) < 0) {
            throw new SellPriceLowerThanBuyPriceException("Sale price cannot be lower than purchase price.");
        }

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
        product.setStock(0);

        Product saved = productRepository.save(product);

        Price price = new Price();
        price.setProduct(saved);
        price.setPurchasePrice(dto.getPrice().getPurchasePrice());
        price.setSalePrice(dto.getPrice().getSalePrice());
        priceRepository.save(price);

        if (dto.getStock() > 0) {
            StockPurchaseItemRequestDTO item = new StockPurchaseItemRequestDTO();
            item.setProductId(saved.getId());
            item.setQuantity(dto.getStock());
            item.setUnitCost(dto.getPrice().getPurchasePrice());

            StockPurchaseRequestDTO purchase = new StockPurchaseRequestDTO();
            purchase.setItems(List.of(item));
            // Joins this transaction: product, price and initial purchase succeed together.
            stockPurchaseService.create(purchase);
        }

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

        if (dto.getStock() != null && dto.getStock() != product.getStock()) {
            throw new DirectStockUpdateException("Para alterar o estoque, registre uma compra ou uma venda. O saldo não pode ser alterado diretamente.");
        }

        Brand targetBrand = dto.getBrandId() != null
                ? getAuthorizedBrand(dto.getBrandId(), currentUser)
                : product.getBrand();

        String targetBrandCode = dto.getBrandCode() != null
                ? dto.getBrandCode()
                : product.getBrandCode();

        if (dto.getBrandId() != null || dto.getBrandCode() != null) {
            validateBrandCode(targetBrand, targetBrandCode, product.getId());
            product.setBrand(targetBrand);
            product.setBrandCode(targetBrandCode);
        }

        if (dto.getCategoryId() != null) {
            Category category = getAuthorizedCategory(dto.getCategoryId(), currentUser);
            product.setCategory(category);
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {product.setName(dto.getName());}
        if (dto.getDescription() != null) {product.setDescription(dto.getDescription());}
        if (dto.getImageUrl() != null) {product.setImageUrl(dto.getImageUrl());}

        return toResponseDTO(product);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Product product = getAuthorizedProduct(id, currentUser);

        product.setDeletedAt(LocalDateTime.now());
    }

}

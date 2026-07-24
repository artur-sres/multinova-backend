package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Price;
import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.price.PriceRequestDTO;
import com.artur_sres.multinova.dto.price.PriceResponseDTO;
import com.artur_sres.multinova.exception.AccessDeniedException;
import com.artur_sres.multinova.exception.PriceNotFoundException;
import com.artur_sres.multinova.exception.ProductNotFoundException;
import com.artur_sres.multinova.exception.SellPriceLowerThanBuyPriceException;
import com.artur_sres.multinova.repository.PriceRepository;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PriceService {
    private final PriceRepository priceRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ProductRepository productRepository;

    public PriceService(PriceRepository priceRepository, AuthenticatedUserProvider authenticatedUserProvider, ProductRepository productRepository) {
        this.priceRepository = priceRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.productRepository = productRepository;
    }

    private PriceResponseDTO toResponseDTO(Price price) {
        PriceResponseDTO dto = new PriceResponseDTO();
        dto.setId(price.getId());
        dto.setProductId(price.getProduct().getId());
        dto.setPurchasePrice(price.getPurchasePrice());
        dto.setSalePrice(price.getSalePrice());
        dto.setCreatedAt(price.getCreatedAt());
        return dto;
    }

    private Product getAuthorizedProduct(Long productId) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));

        if (!currentUser.getId().equals(product.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        return product;
    }

    @Transactional
    public PriceResponseDTO create(Long productId, PriceRequestDTO dto) {
        if (dto.getSalePrice().compareTo(dto.getPurchasePrice()) < 0) {
            throw new SellPriceLowerThanBuyPriceException("Sale price cannot be lower than purchase price.");
        }

        Product product = getAuthorizedProduct(productId);

        Price price = new Price();
        price.setProduct(product);
        price.setPurchasePrice(dto.getPurchasePrice());
        price.setSalePrice(dto.getSalePrice());

        Price saved = priceRepository.save(price);
        return toResponseDTO(saved);
    }

    public List<PriceResponseDTO> listByProduct(Long productId) {
        Product product = getAuthorizedProduct(productId);

        List<Price> prices = priceRepository.findByProductIdOrderByCreatedAtDesc(product.getId());

        return prices.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PriceResponseDTO getCurrentPrice(Long productId) {
        Product product = getAuthorizedProduct(productId);

        Price price = priceRepository.findFirstByProductIdOrderByCreatedAtDesc(product.getId())
                .orElseThrow(() -> new PriceNotFoundException("Price not found."));

        return toResponseDTO(price);
    }
}

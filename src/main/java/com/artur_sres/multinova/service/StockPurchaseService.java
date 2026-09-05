package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.StockPurchase;
import com.artur_sres.multinova.domain.StockPurchaseItem;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseRequestDTO;
import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseResponseDTO;
import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemRequestDTO;
import com.artur_sres.multinova.exception.ProductNotFoundException;
import com.artur_sres.multinova.mapper.StockPurchaseMapper;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.repository.StockPurchaseRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StockPurchaseService {
    private final StockPurchaseRepository stockPurchaseRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final StockPurchaseMapper stockPurchaseMapper;

    public StockPurchaseService(StockPurchaseRepository stockPurchaseRepository,
                                ProductRepository productRepository,
                                AuthenticatedUserProvider authenticatedUserProvider,
                                StockPurchaseMapper stockPurchaseMapper) {
        this.stockPurchaseRepository = stockPurchaseRepository;
        this.productRepository = productRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.stockPurchaseMapper = stockPurchaseMapper;
    }

    private Product getAuthorizedProduct(Long id, User user) {
        return productRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
    }

    @Transactional
    public StockPurchaseResponseDTO create(StockPurchaseRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        StockPurchase stockPurchase = new StockPurchase();
        stockPurchase.setUser(currentUser);

        for (StockPurchaseItemRequestDTO itemDTO : dto.getItems()) {
            Product product = getAuthorizedProduct(itemDTO.getProductId(), currentUser);
            product.setStock(product.getStock() + itemDTO.getQuantity());

            StockPurchaseItem item = new StockPurchaseItem();
            item.setStockPurchase(stockPurchase);
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitCost(itemDTO.getUnitCost());
            item.setSubtotal(item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())));

            stockPurchase.addToTotalAmount(item.getSubtotal());
            stockPurchase.addItem(item);
        }

        StockPurchase saved = stockPurchaseRepository.save(stockPurchase);
        return stockPurchaseMapper.toResponseDTO(saved);
    }

    public List<StockPurchaseResponseDTO> listAll() {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        List<StockPurchase> stockPurchases = stockPurchaseRepository.findByUserId(currentUser.getId());

        return stockPurchases.stream()
                .map(stockPurchaseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}

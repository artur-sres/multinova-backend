package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.domain.StockPurchase;
import com.artur_sres.multinova.domain.StockPurchaseItem;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.domain.Bill;
import com.artur_sres.multinova.domain.enums.StockEntryType;
import com.artur_sres.multinova.repository.BillRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    private final BillRepository billRepository;

    public StockPurchaseService(StockPurchaseRepository stockPurchaseRepository,
                                ProductRepository productRepository,
                                AuthenticatedUserProvider authenticatedUserProvider,
                                StockPurchaseMapper stockPurchaseMapper, BillRepository billRepository) {
        this.stockPurchaseRepository = stockPurchaseRepository;
        this.productRepository = productRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.stockPurchaseMapper = stockPurchaseMapper;
        this.billRepository = billRepository;
    }

    private Product getAuthorizedProduct(Long id, User user) {
        return productRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
    }

    @Transactional
    public StockPurchaseResponseDTO create(StockPurchaseRequestDTO dto) {
        if (dto.getEntryType() == null || (dto.getEntryType() == StockEntryType.PENDING && dto.getDueDate() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o tipo de entrada e o vencimento da compra a prazo.");
        }
        User currentUser = authenticatedUserProvider.getCurrentUser();

        StockPurchase stockPurchase = new StockPurchase();
        stockPurchase.setUser(currentUser);
        stockPurchase.setEntryType(dto.getEntryType());

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
        if (dto.getEntryType() == StockEntryType.PENDING) {
            Bill bill = new Bill();
            bill.setUser(currentUser);
            bill.setName("Compra de estoque #" + saved.getId());
            bill.setDescription("Entrada de estoque a prazo. " + saved.getItems().stream()
                    .map(item -> item.getQuantity() + " x " + item.getProduct().getName())
                    .collect(Collectors.joining(", ")));
            bill.setAmount(saved.getTotalAmount());
            bill.setDueDate(dto.getDueDate());
            billRepository.save(bill);
        }
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

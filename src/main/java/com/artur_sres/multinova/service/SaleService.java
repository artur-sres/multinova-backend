package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.*;
import com.artur_sres.multinova.domain.enums.SaleStatus;
import com.artur_sres.multinova.dto.sale.SaleRequestDTO;
import com.artur_sres.multinova.dto.sale.SaleResponseDTO;
import com.artur_sres.multinova.dto.sale_item.SaleItemRequestDTO;
import com.artur_sres.multinova.exception.CustomerNotFoundException;
import com.artur_sres.multinova.exception.InsufficientStockException;
import com.artur_sres.multinova.exception.ProductNotFoundException;
import com.artur_sres.multinova.exception.SaleNotFoundException;
import com.artur_sres.multinova.mapper.SaleMapper;
import com.artur_sres.multinova.repository.CustomerRepository;
import com.artur_sres.multinova.repository.ProductRepository;
import com.artur_sres.multinova.repository.SaleRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SaleService {
    private final SaleRepository saleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;

    public SaleService(SaleRepository saleRepository,
                       AuthenticatedUserProvider authenticatedUserProvider,
                       CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       SaleMapper saleMapper) {
        this.saleRepository = saleRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.saleMapper = saleMapper;
    }

    private Customer getAuthorizedCustomer(Long id, User currenUser) {
        return customerRepository.findByIdAndUserId(id, currenUser.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
    }

    private Product getAuthorizedProduct(Long productId, User currentUser) {
        return productRepository.findByIdAndUserIdAndDeletedAtIsNull(productId, currentUser.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found."));
    }

    private Sale getAuthorizedSale(Long saleId, User currentUser, SaleStatus status) {
        return saleRepository.findByIdAndUserIdAndStatus(saleId, currentUser.getId(), status)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found."));
    }

    @Transactional
    public SaleResponseDTO create(SaleRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Customer currentCustomer = getAuthorizedCustomer(dto.getCustomerId(), currentUser);

        Sale sale = new Sale();

        sale.setUser(currentUser);
        sale.setCustomer(currentCustomer);

        for(SaleItemRequestDTO saleItemRequestDTO : dto.getItems()) {
            Product product = getAuthorizedProduct(saleItemRequestDTO.getProductId(), currentUser);

            int quantity = saleItemRequestDTO.getQuantity();
            if (product.getStock() < quantity) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setProductName(product.getName());
            saleItem.setQuantity(saleItemRequestDTO.getQuantity());
            saleItem.setUnitPrice(saleItemRequestDTO.getUnitPrice());
            BigDecimal gross = saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getQuantity()));
            saleItem.setDiscountType(saleItemRequestDTO.getDiscountType());
            saleItem.setDiscountValue(saleItemRequestDTO.getDiscountValue());
            saleItem.setDiscountAmount(SaleDiscount.amount(gross, saleItem.getDiscountType(), saleItem.getDiscountValue()));
            saleItem.setSubtotal(gross.subtract(saleItem.getDiscountAmount()));
            product.setStock(product.getStock() - quantity);

            sale.addToTotalAmount(saleItem.getSubtotal());
            sale.addItem(saleItem);
        }

        BigDecimal debtBalance = currentCustomer.getDebtBalance() != null
                ? currentCustomer.getDebtBalance() : BigDecimal.ZERO;
        currentCustomer.setDebtBalance(debtBalance.add(sale.getTotalAmount()));

        Sale saved = saleRepository.save(sale);

        return saleMapper.toResponseDTO(saved);
    }

    @Transactional
    public void cancel(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Sale sale = getAuthorizedSale(id, currentUser, SaleStatus.COMPLETED);

        for(SaleItem saleItem : sale.getItems()) {
            Product product = saleItem.getProduct();
            product.addToStock(saleItem.getQuantity());
        }

        Customer customer = sale.getCustomer();
        BigDecimal debtBalance = customer.getDebtBalance() != null
                ? customer.getDebtBalance() : BigDecimal.ZERO;
        customer.setDebtBalance(debtBalance.subtract(sale.getTotalAmount()));

        sale.setStatus(SaleStatus.CANCELLED);
    }

    public List<SaleResponseDTO> listAll(SaleStatus status) {
        User currentUser = authenticatedUserProvider.getCurrentUser();


        List<Sale> sales = (status != null)
                ? saleRepository.findByUserIdAndStatus(currentUser.getId(), status)
                : saleRepository.findByUserId(currentUser.getId());

        return sales.stream()
                .map(saleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}

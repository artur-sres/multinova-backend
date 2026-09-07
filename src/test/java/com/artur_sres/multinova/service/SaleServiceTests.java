package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.*;
import com.artur_sres.multinova.domain.enums.SaleStatus;
import com.artur_sres.multinova.domain.enums.DiscountType;
import com.artur_sres.multinova.dto.sale.SaleRequestDTO;
import com.artur_sres.multinova.dto.sale_item.SaleItemRequestDTO;
import com.artur_sres.multinova.exception.SaleNotFoundException;
import com.artur_sres.multinova.mapper.SaleMapper;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SaleServiceTests {
    @ParameterizedTest
    @ValueSource(strings = {"AMOUNT", "PERCENTAGE"})
    void discountedSaleAndCancellationUseNetTotal(String discountType) {
        var sales = mock(SaleRepository.class);
        var customers = mock(CustomerRepository.class);
        var products = mock(ProductRepository.class);
        var auth = mock(AuthenticatedUserProvider.class);
        var user = new User(); user.setId(1L);
        when(auth.getCurrentUser()).thenReturn(user);
        var customer = new Customer(); customer.setDebtBalance(new BigDecimal("5.00"));
        when(customers.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(customer));
        var product = new Product(); product.setStock(10); product.setName("Café");
        when(products.findByIdAndUserIdAndDeletedAtIsNull(3L, 1L)).thenReturn(Optional.of(product));
        when(sales.save(any())).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            assertEquals(new BigDecimal("22.50"), sale.getTotalAmount());
            assertEquals(new BigDecimal("2.50"), sale.getItems().getFirst().getDiscountAmount());
            assertEquals(new BigDecimal("12.50"), sale.getItems().getFirst().getUnitPrice());
            when(sales.findByIdAndUserIdAndStatus(4L, 1L, SaleStatus.COMPLETED)).thenReturn(Optional.of(sale));
            return sale;
        });
        var item = new SaleItemRequestDTO(); item.setProductId(3L); item.setQuantity(2); item.setUnitPrice(new BigDecimal("12.50"));
        item.setDiscountType(DiscountType.valueOf(discountType));
        item.setDiscountValue(new BigDecimal(discountType.equals("AMOUNT") ? "2.50" : "10"));
        var request = new SaleRequestDTO(); request.setCustomerId(2L); request.setItems(List.of(item));
        var service = new SaleService(sales, auth, customers, products, mock(SaleMapper.class));
        service.create(request);
        assertEquals(new BigDecimal("27.50"), customer.getDebtBalance());
        assertEquals(8, product.getStock());
        service.cancel(4L);
        assertEquals(new BigDecimal("5.00"), customer.getDebtBalance());
        assertEquals(10, product.getStock());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0.00", "100.00", "-100.00", "-25.00"})
    void creationAndCancellationUpdateDebtAndRestoreStock(String initialBalance) {
        SaleRepository sales = mock(SaleRepository.class);
        CustomerRepository customers = mock(CustomerRepository.class);
        ProductRepository products = mock(ProductRepository.class);
        AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
        SaleService service = new SaleService(sales, auth, customers, products, mock(SaleMapper.class));

        User user = new User();
        user.setId(1L);
        when(auth.getCurrentUser()).thenReturn(user);
        Customer customer = new Customer();
        customer.setDebtBalance(initialBalance == null ? null : new BigDecimal(initialBalance));
        when(customers.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(customer));
        Product product = new Product();
        product.setStock(10);
        when(products.findByIdAndUserIdAndDeletedAtIsNull(3L, 1L)).thenReturn(Optional.of(product));
        when(sales.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            when(sales.findByIdAndUserIdAndStatus(4L, 1L, SaleStatus.COMPLETED))
                    .thenAnswer(lookup -> sale.getStatus() == SaleStatus.COMPLETED
                            ? Optional.of(sale) : Optional.empty());
            return sale;
        });

        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductId(3L);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("12.50"));
        SaleRequestDTO request = new SaleRequestDTO();
        request.setCustomerId(2L);
        request.setItems(List.of(item, item));

        BigDecimal initial = initialBalance == null ? BigDecimal.ZERO : new BigDecimal(initialBalance);
        service.create(request);
        assertEquals(0, initial.add(new BigDecimal("50.00")).compareTo(customer.getDebtBalance()));
        assertEquals(6, product.getStock());

        service.cancel(4L);
        assertEquals(0, initial.compareTo(customer.getDebtBalance()));
        assertEquals(10, product.getStock());

        assertThrows(SaleNotFoundException.class, () -> service.cancel(4L));
        assertEquals(0, initial.compareTo(customer.getDebtBalance()));
        assertEquals(10, product.getStock());
    }
}

package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.*;
import com.artur_sres.multinova.domain.enums.StockEntryType;
import com.artur_sres.multinova.domain.enums.BillStatus;
import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseRequestDTO;
import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemRequestDTO;
import com.artur_sres.multinova.exception.ProductNotFoundException;
import com.artur_sres.multinova.mapper.StockPurchaseMapper;
import com.artur_sres.multinova.mapper.StockPurchaseMapperImpl;
import com.artur_sres.multinova.mapper.StockPurchaseItemMapperImpl;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StockPurchaseServiceTests {
    @Test
    void entryTypeControlsPaymentAndPendingBill() {
        for (StockEntryType type : StockEntryType.values()) {
            var purchases = mock(StockPurchaseRepository.class);
            var products = mock(ProductRepository.class);
            var bills = mock(BillRepository.class);
            var auth = mock(AuthenticatedUserProvider.class);
            var service = new StockPurchaseService(purchases, products, auth, mock(StockPurchaseMapper.class), bills);
            var user = new User(); user.setId(1L);
            when(auth.getCurrentUser()).thenReturn(user);
            var product = new Product(); product.setName("Café"); product.setStock(5);
            when(products.findByIdAndUserIdAndDeletedAtIsNull(2L, 1L)).thenReturn(Optional.of(product));
            when(purchases.save(any())).thenAnswer(invocation -> {
                StockPurchase purchase = invocation.getArgument(0);
                purchase.setId(12L);
                assertEquals(type, purchase.getEntryType());
                return purchase;
            });
            var item = new StockPurchaseItemRequestDTO();
            item.setProductId(2L); item.setQuantity(3); item.setUnitCost(new BigDecimal("12.50"));
            var request = new StockPurchaseRequestDTO();
            assertEquals(StockEntryType.ADJUSTMENT, request.getEntryType());
            request.setItems(List.of(item)); request.setEntryType(type);
            request.setDueDate(java.time.LocalDate.of(2026, 10, 1));
            service.create(request);
            assertEquals(8, product.getStock());
            if (type == StockEntryType.PENDING) {
                var captor = org.mockito.ArgumentCaptor.forClass(Bill.class);
                verify(bills).save(captor.capture());
                Bill bill = captor.getValue();
                assertSame(user, bill.getUser());
                assertEquals(new BigDecimal("37.50"), bill.getAmount());
                assertEquals(request.getDueDate(), bill.getDueDate());
                assertEquals(BillStatus.PENDING, bill.getStatus());
                assertNull(bill.getPaidAt());
                assertTrue(bill.getName().contains("12"));
            } else {
                verifyNoInteractions(bills);
            }
        }
    }

    @Test
    void pendingEntryRequiresDueDateBeforeChangingStock() {
        var purchases = mock(StockPurchaseRepository.class);
        var products = mock(ProductRepository.class);
        var bills = mock(BillRepository.class);
        var auth = mock(AuthenticatedUserProvider.class);
        var service = new StockPurchaseService(purchases, products, auth, mock(StockPurchaseMapper.class), bills);
        var request = new StockPurchaseRequestDTO(); request.setEntryType(StockEntryType.PENDING);
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.create(request));
        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(purchases, products, bills, auth);
    }

    @Test
    void creationAddsStockAndMapsItemsAndListing() {
        try (var context = new AnnotationConfigApplicationContext(
                StockPurchaseMapperImpl.class, StockPurchaseItemMapperImpl.class)) {
            StockPurchaseRepository purchases = mock(StockPurchaseRepository.class);
            ProductRepository products = mock(ProductRepository.class);
            AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
            StockPurchaseService service = new StockPurchaseService(
                    purchases, products, auth, context.getBean(StockPurchaseMapper.class), mock(BillRepository.class));
            User user = new User();
            user.setId(1L);
            when(auth.getCurrentUser()).thenReturn(user);
            Product product = new Product();
            product.setId(2L);
            product.setName("Product");
            product.setStock(10);
            when(products.findByIdAndUserIdAndDeletedAtIsNull(2L, 1L))
                    .thenReturn(Optional.of(product));
            when(purchases.save(any(StockPurchase.class))).thenAnswer(invocation -> {
                StockPurchase purchase = invocation.getArgument(0);
                assertSame(user, purchase.getUser());
                purchase.getItems().forEach(item -> assertSame(purchase, item.getStockPurchase()));
                when(purchases.findByUserId(1L)).thenReturn(List.of(purchase));
                return purchase;
            });

            StockPurchaseItemRequestDTO item = new StockPurchaseItemRequestDTO();
            item.setProductId(2L);
            item.setQuantity(3);
            item.setUnitCost(new BigDecimal("12.50"));
            StockPurchaseRequestDTO request = new StockPurchaseRequestDTO();
            request.setItems(List.of(item, item));

            var response = service.create(request);
            assertEquals(16, product.getStock());
            assertEquals(new BigDecimal("75.00"), response.getTotalAmount());
            assertEquals(StockEntryType.ADJUSTMENT, response.getEntryType());
            assertEquals(2, response.getItems().size());
            response.getItems().forEach(result -> {
                assertEquals(2L, result.getProductId());
                assertEquals("Product", result.getProductName());
                assertEquals(new BigDecimal("37.50"), result.getSubtotal());
            });
            assertEquals("Product", service.listAll().getFirst().getItems().getFirst().getProductName());
            verify(products, never()).save(any(Product.class));

            when(products.findByIdAndUserIdAndDeletedAtIsNull(2L, 1L))
                    .thenReturn(Optional.empty());
            assertThrows(ProductNotFoundException.class, () -> service.create(request));
            assertEquals(16, product.getStock());
            verify(purchases, times(1)).save(any(StockPurchase.class));
        }
    }
}

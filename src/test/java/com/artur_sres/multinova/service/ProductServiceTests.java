package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.*;
import com.artur_sres.multinova.dto.price.PriceRequestDTO;
import com.artur_sres.multinova.dto.product.ProductPatchDTO;
import com.artur_sres.multinova.dto.product.ProductRequestDTO;
import com.artur_sres.multinova.exception.DirectStockUpdateException;
import com.artur_sres.multinova.exception.GlobalExceptionHandler;
import com.artur_sres.multinova.mapper.*;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTests {
    private static class Fixture implements AutoCloseable {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                ProductMapperImpl.class, StockPurchaseMapperImpl.class, StockPurchaseItemMapperImpl.class);
        final ProductRepository products = mock(ProductRepository.class);
        final PriceRepository prices = mock(PriceRepository.class);
        final StockPurchaseRepository purchases = mock(StockPurchaseRepository.class);
        final AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
        final ProductService service;
        StockPurchase recordedPurchase;

        Fixture() {
            User user = new User();
            user.setId(1L);
            when(auth.getCurrentUser()).thenReturn(user);
            when(products.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                assertEquals(0, product.getStock(), "Stock must be added by the purchase flow only");
                product.setId(2L);
                when(products.findByIdAndUserIdAndDeletedAtIsNull(2L, 1L)).thenReturn(Optional.of(product));
                return product;
            });
            when(prices.save(any(Price.class))).thenAnswer(invocation -> {
                Price price = invocation.getArgument(0);
                when(prices.findFirstByProductIdOrderByCreatedAtDesc(2L)).thenReturn(Optional.of(price));
                return price;
            });
            when(purchases.save(any(StockPurchase.class))).thenAnswer(invocation -> {
                recordedPurchase = invocation.getArgument(0);
                return recordedPurchase;
            });
            StockPurchaseService stockService = new StockPurchaseService(
                    purchases, products, auth, context.getBean(StockPurchaseMapper.class));
            service = new ProductService(products, mock(BrandRepository.class), mock(CategoryRepository.class),
                    auth, context.getBean(ProductMapper.class), prices, stockService);
        }

        ProductRequestDTO request(int stock, String unitCost) {
            PriceRequestDTO price = new PriceRequestDTO();
            price.setPurchasePrice(new BigDecimal(unitCost));
            price.setSalePrice(new BigDecimal("20.00"));
            ProductRequestDTO request = new ProductRequestDTO();
            request.setName("Café");
            request.setStock(stock);
            request.setPrice(price);
            return request;
        }

        @Override
        public void close() { context.close(); }
    }

    @Test
    void initialStockRecordsOnePurchaseAndIsIncludedInThePeriodBalance() {
        try (var fixture = new Fixture()) {
            var product = fixture.service.create(fixture.request(3, "12.50"));
            assertEquals(3, product.getStock());
            assertEquals(new BigDecimal("12.50"), product.getPurchasePrice());
            var purchase = fixture.recordedPurchase;
            assertNotNull(purchase);
            assertEquals(1L, purchase.getUser().getId());
            assertEquals(new BigDecimal("37.50"), purchase.getTotalAmount());
            assertEquals(1, purchase.getItems().size());
            var item = purchase.getItems().getFirst();
            assertEquals(product.getId(), item.getProduct().getId());
            assertEquals(3, item.getQuantity());
            assertEquals(new BigDecimal("12.50"), item.getUnitCost());
            assertSame(purchase, item.getStockPurchase());
            verify(fixture.purchases, times(1)).save(any(StockPurchase.class));

            var from = LocalDate.of(2026, 9, 5).atStartOfDay();
            var to = from.plusDays(1).minusSeconds(1);
            var sales = mock(SaleRepository.class);
            var payments = mock(CustomerPaymentRepository.class);
            var bills = mock(BillRepository.class);
            var customers = mock(CustomerRepository.class);
            when(sales.sumCompletedByUserAndPeriod(1L, from, to)).thenReturn(BigDecimal.ZERO);
            when(payments.sumByUserAndPeriod(1L, from, to)).thenReturn(BigDecimal.ZERO);
            when(bills.sumPaidByUserAndPeriod(1L, from, to)).thenReturn(BigDecimal.ZERO);
            when(fixture.purchases.sumByUserAndPeriod(1L, from, to)).thenReturn(purchase.getTotalAmount());
            var dashboard = new DashboardService(sales, payments, fixture.purchases, bills, customers, fixture.auth);
            var summary = dashboard.getSummary(from, to);
            assertEquals(new BigDecimal("37.50"), summary.getPurchased());
            assertEquals(new BigDecimal("-37.50"), summary.getPeriodBalance());
        }
    }

    @Test
    void zeroStockDoesNotCreateAPurchase() {
        try (var fixture = new Fixture()) {
            assertEquals(0, fixture.service.create(fixture.request(0, "12.50")).getStock());
            verify(fixture.purchases, never()).save(any(StockPurchase.class));
        }
    }

    @Test
    void zeroCostStillRecordsTheInitialQuantity() {
        try (var fixture = new Fixture()) {
            assertEquals(4, fixture.service.create(fixture.request(4, "0.00")).getStock());
            assertEquals(new BigDecimal("0.00"), fixture.recordedPurchase.getTotalAmount());
            assertEquals(4, fixture.recordedPurchase.getItems().getFirst().getQuantity());
        }
    }

    @Test
    void purchaseFailureRollsBackTheProductCreationTransaction() {
        try (var fixture = new Fixture()) {
            when(fixture.purchases.save(any(StockPurchase.class))).thenThrow(new IllegalStateException("Purchase failed"));
            var transactions = mock(PlatformTransactionManager.class);
            var status = mock(TransactionStatus.class);
            when(transactions.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
            var interceptor = new TransactionInterceptor();
            interceptor.setTransactionManager(transactions);
            interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
            var factory = new ProxyFactory(fixture.service);
            factory.addAdvice(interceptor);
            ProductService proxy = (ProductService) factory.getProxy();

            assertThrows(IllegalStateException.class, () -> proxy.create(fixture.request(3, "12.50")));
            verify(transactions).rollback(status);
            verify(transactions, never()).commit(any());
        }
    }

    @Test
    void patchCannotBypassPurchaseOrSaleAndReturnsConflict() {
        try (var fixture = new Fixture()) {
            fixture.service.create(fixture.request(3, "12.50"));
            for (int stock : new int[]{4, 2}) {
                ProductPatchDTO patch = new ProductPatchDTO();
                patch.setStock(stock);
                patch.setName("Must not change");
                var error = assertThrows(DirectStockUpdateException.class, () -> fixture.service.update(2L, patch));
                assertEquals(409, new GlobalExceptionHandler().handleDirectStockUpdate(error).getStatusCode().value());
                assertEquals(3, fixture.service.searchById(2L).getStock());
                assertEquals("Café", fixture.service.searchById(2L).getName());
            }
            ProductPatchDTO unchangedStock = new ProductPatchDTO();
            unchangedStock.setStock(3);
            unchangedStock.setName("Novo nome");
            assertEquals("Novo nome", fixture.service.update(2L, unchangedStock).getName());
            verify(fixture.purchases, times(1)).save(any(StockPurchase.class));
        }
    }
}

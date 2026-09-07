package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(named = "MULTINOVA_STATEMENT_TEST_URL", matches = ".+")
class CustomerStatementIntegrationTests {
    @Test void snapshotsDetailedBalancesReversalsAndReceiptOwnership() {
        var ds = new DriverManagerDataSource(System.getenv("MULTINOVA_STATEMENT_TEST_URL"), "postgres", "");
        var jdbc = new JdbcTemplate(ds);
        new TransactionTemplate(new DataSourceTransactionManager(ds)).executeWithoutResult(tx -> {
            tx.setRollbackOnly();
            Long userId = jdbc.queryForObject("INSERT INTO users(name,email,password) VALUES ('Loja',?,'test') RETURNING id", Long.class, UUID.randomUUID() + "@test.invalid");
            Long customer = jdbc.queryForObject("INSERT INTO customers(user_id,name,debt_balance) VALUES (?,'Cliente original',25) RETURNING id", Long.class, userId);
            Long product = jdbc.queryForObject("INSERT INTO products(user_id,name,stock) VALUES (?,'Caderno original',10) RETURNING id", Long.class, userId);
            Long sale = jdbc.queryForObject("INSERT INTO sales(user_id,customer_id,total_amount) VALUES (?,?,30) RETURNING id", Long.class, userId, customer);
            jdbc.update("INSERT INTO sale_items(sale_id,product_id,quantity,unit_price,subtotal) VALUES (?,?,2,15,30)", sale, product);
            jdbc.update("UPDATE customers SET debt_balance=55 WHERE id=?", customer);
            Long payment = jdbc.queryForObject("INSERT INTO customer_payments(customer_id,amount,paid_at) VALUES (?,10,'2026-01-01 12:00:00') RETURNING id", Long.class, customer);
            jdbc.update("UPDATE customers SET name='Cliente renomeado', debt_balance=45 WHERE id=?", customer);
            jdbc.update("UPDATE products SET name='Caderno renomeado' WHERE id=?", product);
            var auth = mock(AuthenticatedUserProvider.class);
            var user = new User(); user.setId(userId); user.setName("Loja"); when(auth.getCurrentUser()).thenReturn(user);
            var service = new CustomerStatementService(jdbc, auth);
            var page = service.statement(customer, 0);
            assertEquals(3, page.items().size());
            var bought = page.items().stream().filter(e -> e.type().equals("SALE")).findFirst().orElseThrow();
            assertEquals("Cliente original", bought.customerName());
            assertEquals("Caderno original", bought.items().getFirst().productName());
            assertFalse(bought.items().getFirst().nameRecovered());
            assertEquals(2, bought.items().getFirst().quantity());
            assertEquals(new BigDecimal("25.00"), bought.balanceBefore());
            assertEquals(new BigDecimal("55.00"), bought.balance());
            var paid = page.items().stream().filter(e -> e.type().equals("PAYMENT")).findFirst().orElseThrow();
            assertEquals(payment.longValue(), paid.sourceId());
            assertEquals(new BigDecimal("45.00"), paid.balance());
            assertEquals("REGISTRADO", service.saleReceipt(sale).status());
            service.reverse(customer, paid.id(), "Lançamento duplicado");
            assertEquals("ESTORNADO", service.receipt(customer, paid.id()).status());
            assertTrue(service.receipt(customer, paid.id()).entry().reversalDescription().contains("Lançamento duplicado"));
            assertThrows(ResponseStatusException.class, () -> service.reverse(customer, paid.id(), "Duplicado"));
            jdbc.update("UPDATE sales SET status='CANCELLED' WHERE id=?", sale);
            assertEquals("CANCELADO", service.saleReceipt(sale).status());
            var reversal = service.statement(customer, 0).items().stream().filter(e -> e.type().equals("REVERSAL")).findFirst().orElseThrow();
            assertEquals("PAYMENT", reversal.originalType());
            assertEquals(paid.id(), reversal.reversedEntryId());
            assertThrows(ResponseStatusException.class, () -> service.receipt(customer, reversal.id()));
            assertThrows(ResponseStatusException.class, () -> service.receipt(customer, Long.MAX_VALUE));
            user.setId(-1L);
            assertThrows(ResponseStatusException.class, () -> service.statement(customer, 0));
            assertThrows(ResponseStatusException.class, () -> service.receipt(customer, paid.id()));
            assertThrows(ResponseStatusException.class, () -> service.saleReceipt(sale));
        });
    }
}

package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.customer.CustomerDebtRequestDTO;
import com.artur_sres.multinova.repository.CustomerPaymentRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Run only against an explicitly supplied disposable PostgreSQL database.
@EnabledIfSystemProperty(named = "statementTestUrl", matches = ".+")
class StatementIntegrationTests {
    @Test
    void migrationsStatementReversalsAndHistoryWorkTogether() throws Exception {
        String schema = "statement_test_" + UUID.randomUUID().toString().replace("-", "");
        var ds = new DriverManagerDataSource(System.getProperty("statementTestUrl"), "postgres", "");
        var admin = new JdbcTemplate(ds);
        admin.execute("CREATE SCHEMA " + schema);
        ds.setUrl(System.getProperty("statementTestUrl") + "?currentSchema=" + schema);
        var jdbc = new JdbcTemplate(ds);
        var tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
        List<Path> migrations;
        try (var files = Files.list(Path.of("src/main/resources/db/migration"))) {
            migrations = files.filter(p -> p.toString().endsWith(".sql"))
                    .sorted(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString().split("__")[0].substring(1)))).toList();
        }
        for (Path migration : migrations) {
            if (migration.getFileName().toString().startsWith("V27__")) {
                jdbc.update("INSERT INTO users(id,name,email,password) VALUES(1,'Owner','owner@test','test'),(2,'Other','other@test','test')");
                jdbc.update("INSERT INTO customers(id,user_id,name,debt_balance) VALUES(10,1,'Legacy',80),(20,2,'Foreign',0)");
                jdbc.update("INSERT INTO customer_payments(customer_id,amount,paid_at) VALUES(10,20,'2026-01-02')");
            }
            if (!Files.readString(migration).isBlank()) jdbc.execute(Files.readString(migration));
        }
        var auth = mock(AuthenticatedUserProvider.class); var user = new User(); user.setId(1L);
        when(auth.getCurrentUser()).thenReturn(user);
        var statement = new CustomerStatementService(jdbc, auth);
        var debt = new CustomerDebtService(jdbc, auth);
        var history = new HistoryService(jdbc, auth);
        var legacy = statement.statement(10, 0).items();
        assertEquals(0, new BigDecimal("80").compareTo(legacy.getFirst().balance()));
        assertEquals(0, new BigDecimal("100").compareTo(legacy.getLast().delta()));
        assertTrue(legacy.getLast().recovered());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> statement.statement(20, 0));

        jdbc.update("INSERT INTO customers(id,user_id,name,debt_balance) VALUES(30,1,'Ana',100)");
        tx.executeWithoutResult(s -> debt.create(30L, new CustomerDebtRequestDTO(new BigDecimal("25"), LocalDate.now().minusDays(1), "Migration")));
        jdbc.update("INSERT INTO customer_payments(customer_id,amount,paid_at) VALUES(30,30,CURRENT_TIMESTAMP - INTERVAL '1 day')");
        jdbc.update("UPDATE customers SET debt_balance = debt_balance - 30 WHERE id = 30");
        jdbc.update("INSERT INTO sales(id,user_id,customer_id,total_amount,status) VALUES(50,1,30,10,'COMPLETED')");
        jdbc.update("UPDATE customers SET debt_balance = debt_balance + 10 WHERE id = 30");
        jdbc.update("UPDATE sales SET status = 'CANCELLED' WHERE id = 50");
        jdbc.update("UPDATE customers SET debt_balance = debt_balance - 10 WHERE id = 30");
        assertEquals(0, new BigDecimal("95").compareTo(statement.statement(30, 0).items().getFirst().balance()));
        var entries = statement.statement(30, 0).items();
        long debitId = entries.stream().filter(e -> e.type().equals("DEBT")).findFirst().orElseThrow().id();
        long paymentId = entries.stream().filter(e -> e.type().equals("PAYMENT")).findFirst().orElseThrow().id();
        long initialId = entries.stream().filter(e -> e.type().equals("INITIAL")).findFirst().orElseThrow().id();
        long saleId = entries.stream().filter(e -> e.type().equals("SALE")).findFirst().orElseThrow().id();
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> tx.executeWithoutResult(s -> statement.reverse(30, saleId, "Wrong action")));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> tx.executeWithoutResult(s -> statement.reverse(10, debitId, "Wrong customer")));

        jdbc.execute("CREATE FUNCTION reject_reversal() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'test failure'; END $$");
        jdbc.execute("CREATE TRIGGER reject_history BEFORE INSERT ON operation_history FOR EACH ROW EXECUTE FUNCTION reject_reversal()");
        assertThrows(org.springframework.dao.DataAccessException.class, () -> tx.executeWithoutResult(s -> statement.reverse(30, debitId, "Rollback")));
        assertEquals(0, new BigDecimal("95").compareTo(jdbc.queryForObject("SELECT debt_balance FROM customers WHERE id = 30", BigDecimal.class)));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM customer_ledger WHERE reversed_entry_id = ?", Integer.class, debitId));
        jdbc.execute("DROP TRIGGER reject_history ON operation_history");
        tx.executeWithoutResult(s -> statement.reverse(30, debitId, "Duplicate migration"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> tx.executeWithoutResult(s -> statement.reverse(30, debitId, "Again")));
        tx.executeWithoutResult(s -> statement.reverse(30, paymentId, "Payment entered by mistake"));
        tx.executeWithoutResult(s -> statement.reverse(30, initialId, "Initial balance entered by mistake"));
        assertEquals(0, BigDecimal.ZERO.compareTo(jdbc.queryForObject("SELECT debt_balance FROM customers WHERE id = 30", BigDecimal.class)));
        assertEquals(0, BigDecimal.ZERO.compareTo(statement.statement(30, 0).items().getFirst().balance()));
        assertTrue(statement.statement(30, 0).items().stream().filter(e -> e.id() == paymentId).findFirst().orElseThrow().reversed());
        assertEquals(4, history.search("reversals", "Ana", null, null, 0).items().size());

        String paymentSum = CustomerPaymentRepository.class.getMethod("sumByUserAndPeriod", Long.class, LocalDateTime.class, LocalDateTime.class).getAnnotation(Query.class).value();
        var named = new NamedParameterJdbcTemplate(jdbc);
        BigDecimal today = named.queryForObject(paymentSum, Map.of("userId", 1L, "from", LocalDate.now().atStartOfDay(), "to", LocalDate.now().atTime(23,59,59)), BigDecimal.class);
        assertEquals(0, new BigDecimal("-30").compareTo(today));

        jdbc.update("INSERT INTO operation_history(user_id,type,title,entity_id,occurred_at) SELECT 1,'PRODUCT_UPDATED','Search 100% ' || n, n, '2026-02-03'::timestamp FROM generate_series(1,130) n");
        jdbc.update("INSERT INTO operation_history(user_id,type,title,entity_id) VALUES(2,'PRODUCT_UPDATED','Search 100% foreign',1)");
        var ids = new HashSet<Long>(); int page = 0;
        while (true) {
            var result = history.search("records", "100%", LocalDate.of(2026,2,3), LocalDate.of(2026,2,3), page++);
            result.items().forEach(e -> assertTrue(ids.add(e.id())));
            if (!result.hasMore()) break;
        }
        assertEquals(130, ids.size());
        assertTrue(history.search("records", "100%", LocalDate.of(2026,2,4), null, 0).items().isEmpty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> history.search("unknown", "", null, null, 0));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> history.search("all", "", LocalDate.now(), LocalDate.now().minusDays(1), 0));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> statement.statement(30, -1));
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> statement.reverse(30, debitId, " "));
        for (int i = 0; i < 30; i++) tx.executeWithoutResult(s -> debt.create(30L, new CustomerDebtRequestDTO(new BigDecimal("0.10"), LocalDate.now(), "Paging")));
        var firstPage = statement.statement(30, 0);
        assertEquals(25, firstPage.items().size());
        assertTrue(firstPage.hasMore());
        assertEquals(0, new BigDecimal("3.00").compareTo(firstPage.items().getFirst().balance()));
        var secondPage = statement.statement(30, 1);
        assertFalse(secondPage.hasMore());
        assertEquals("INITIAL", secondPage.items().getLast().type());
        var statementIds = new HashSet<Long>();
        firstPage.items().forEach(e -> assertTrue(statementIds.add(e.id())));
        secondPage.items().forEach(e -> assertTrue(statementIds.add(e.id())));
    }
}

package com.artur_sres.multinova.service;

import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Service
public class CustomerStatementService {
    public record Item(long productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal subtotal, boolean nameRecovered) {}
    public record Entry(long id, String type, String description, BigDecimal delta, BigDecimal balance,
                        LocalDateTime effectiveAt, LocalDateTime recordedAt, boolean recovered, boolean reversed, boolean reversible,
                        long sourceId, Long reversedEntryId, Long reversalId, String reversalDescription, String originalType,
                        String saleStatus, String customerName, boolean nameRecovered, List<Item> items) {
        public BigDecimal balanceBefore() { return balance.subtract(delta); }
        public Entry withItems(List<Item> values) {
            return new Entry(id, type, description, delta, balance, effectiveAt, recordedAt, recovered, reversed, reversible,
                    sourceId, reversedEntryId, reversalId, reversalDescription, originalType, saleStatus, customerName, nameRecovered, values);
        }
    }
    public record Receipt(String reference, String issuer, String status, LocalDateTime issuedAt, BigDecimal balanceBefore, Entry entry) {}
    public record Page(List<Entry> items, boolean hasMore, int page) {}
    private final JdbcTemplate jdbc;
    private final AuthenticatedUserProvider auth;
    public CustomerStatementService(JdbcTemplate jdbc, AuthenticatedUserProvider auth) { this.jdbc = jdbc; this.auth = auth; }

    @Transactional(readOnly = true)
    public Page statement(long customerId, int page) {
        if (page < 0 || page > 1000000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página inválida.");
        authorize(customerId, false);
        var entries = entries(customerId, " ORDER BY CASE WHEN m.type = 'INITIAL' THEN 0 ELSE 1 END DESC, m.recorded_at DESC, m.id DESC LIMIT 26 OFFSET ?", page * 25);
        return new Page(entries.stream().limit(25).toList(), entries.size() > 25, page);
    }

    private List<Entry> entries(long customerId, String suffix, Object value) {
        var entries = jdbc.query("""
                WITH movements AS (
                    SELECT l.*, SUM(delta) OVER (ORDER BY CASE WHEN type = 'INITIAL' THEN 0 ELSE 1 END, recorded_at, id) AS balance,
                           EXISTS(SELECT 1 FROM customer_ledger r WHERE r.reversed_entry_id = l.id) AS reversed
                    FROM customer_ledger l WHERE customer_id = ?
                ) SELECT m.*, r.id AS reversal_id, r.description AS reversal_description, original.type AS original_type, s.status AS sale_status
                FROM movements m
                LEFT JOIN customer_ledger r ON r.reversed_entry_id = m.id AND r.customer_id = m.customer_id
                LEFT JOIN customer_ledger original ON original.id = m.reversed_entry_id AND original.customer_id = m.customer_id
                LEFT JOIN sales s ON m.type IN ('SALE','SALE_CANCELLED') AND s.id = m.source_id AND s.customer_id = m.customer_id
                """ + suffix, (rs, row) -> new Entry(rs.getLong("id"), rs.getString("type"), rs.getString("description"),
                rs.getBigDecimal("delta"), rs.getBigDecimal("balance"), rs.getTimestamp("effective_at").toLocalDateTime(),
                rs.getTimestamp("recorded_at").toLocalDateTime(), rs.getBoolean("recovered"), rs.getBoolean("reversed"),
                List.of("INITIAL", "DEBT", "PAYMENT").contains(rs.getString("type")) && !rs.getBoolean("reversed")
                        && rs.getBigDecimal("delta").signum() != 0,
                rs.getLong("source_id"), (Long) rs.getObject("reversed_entry_id"), (Long) rs.getObject("reversal_id"),
                rs.getString("reversal_description"), rs.getString("original_type"), rs.getString("sale_status"),
                rs.getString("customer_name"), rs.getBoolean("name_recovered"), List.of()), customerId, value);
        var saleIds = entries.stream().filter(e -> List.of("SALE", "SALE_CANCELLED").contains(e.type())).map(Entry::sourceId).distinct().toList();
        Map<Long, List<Item>> items = new HashMap<>();
        if (!saleIds.isEmpty()) {
            new NamedParameterJdbcTemplate(jdbc).query("""
                    SELECT i.* FROM sale_items i JOIN sales s ON s.id = i.sale_id
                    WHERE s.customer_id = :customer AND i.sale_id IN (:ids) ORDER BY i.id
                    """, Map.of("customer", customerId, "ids", saleIds), (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                    items.computeIfAbsent(rs.getLong("sale_id"), key -> new ArrayList<>()).add(new Item(rs.getLong("product_id"),
                            rs.getString("product_name"), rs.getInt("quantity"), rs.getBigDecimal("unit_price"),
                            rs.getBigDecimal("subtotal"), rs.getBoolean("name_recovered"))));
        }
        return entries.stream().map(e -> e.withItems(List.of("SALE", "SALE_CANCELLED").contains(e.type())
                ? items.getOrDefault(e.sourceId(), List.of()) : List.of())).toList();
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Receipt receipt(long customerId, long entryId) {
        authorize(customerId, false);
        var values = entries(customerId, " WHERE m.id = ?", entryId);
        if (values.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento não encontrado.");
        Entry entry = values.getFirst();
        if (!List.of("SALE", "PAYMENT").contains(entry.type()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Comprovantes disponíveis para compras e pagamentos.");
        String status = entry.reversed() ? "ESTORNADO" : "CANCELLED".equals(entry.saleStatus()) ? "CANCELADO" : "REGISTRADO";
        return new Receipt((entry.type().equals("SALE") ? "COMPRA-" : "PAGAMENTO-") + entry.sourceId(),
                auth.getCurrentUser().getName(), status, LocalDateTime.now(), entry.balanceBefore(), entry);
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Receipt saleReceipt(long saleId) {
        var rows = jdbc.queryForList("""
                SELECT l.id, l.customer_id FROM customer_ledger l JOIN sales s ON s.id = l.source_id
                WHERE l.type = 'SALE' AND s.id = ? AND s.user_id = ? AND l.customer_id = s.customer_id
                """, saleId, auth.getCurrentUser().getId());
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra não encontrada.");
        return receipt(((Number) rows.getFirst().get("customer_id")).longValue(), ((Number) rows.getFirst().get("id")).longValue());
    }

    private void authorize(long customerId, boolean lock) {
        var ids = jdbc.queryForList("SELECT id FROM customers WHERE id = ? AND user_id = ?" + (lock ? " FOR UPDATE" : ""),
                Long.class, customerId, auth.getCurrentUser().getId());
        if (ids.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado.");
    }

    @Transactional
    public void reverse(long customerId, long entryId, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 1000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o motivo do estorno, com até 1.000 caracteres.");
        authorize(customerId, true);
        var originals = jdbc.queryForList("SELECT * FROM customer_ledger WHERE id = ? AND customer_id = ?", entryId, customerId);
        if (originals.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento não encontrado.");
        var original = originals.getFirst();
        String type = (String) original.get("type");
        BigDecimal delta = (BigDecimal) original.get("delta");
        if (!List.of("INITIAL", "DEBT", "PAYMENT").contains(type) || delta.signum() == 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este lançamento não pode ser estornado. Para vendas, use o cancelamento da venda.");
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM customer_ledger WHERE reversed_entry_id = ?)", Boolean.class, entryId)))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este lançamento já foi estornado.");
        BigDecimal current = jdbc.queryForObject("SELECT COALESCE(debt_balance, 0) FROM customers WHERE id = ?", BigDecimal.class, customerId);
        if (current.subtract(delta).abs().compareTo(new BigDecimal("99999999.99")) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O saldo após o estorno ultrapassaria o limite permitido.");
        jdbc.update("UPDATE customers SET debt_balance = COALESCE(debt_balance, 0) - ?, version = version + 1, updated_at = clock_timestamp() WHERE id = ?", delta, customerId);
        jdbc.update("""
                INSERT INTO customer_ledger(customer_id, type, source_id, description, delta, effective_at, reversed_entry_id)
                VALUES (?, 'REVERSAL', ?, ?, ?, clock_timestamp(), ?)
                """, customerId, entryId, "Estorno: " + original.get("description") + " — Motivo: " + reason.trim(), delta.negate(), entryId);
        String historyType = switch (type) { case "PAYMENT" -> "CUSTOMER_PAYMENT_REVERSED"; case "DEBT" -> "CUSTOMER_DEBT_REVERSED"; default -> "CUSTOMER_INITIAL_DEBT_REVERSED"; };
        jdbc.update("""
                INSERT INTO operation_history(user_id, type, title, entity_id, amount)
                SELECT user_id, ?, name || ' — Estorno: ' || ? || ' — Motivo: ' || ?, id, ? FROM customers WHERE id = ?
                """, historyType, original.get("description"), reason.trim(), delta.abs(), customerId);
    }
}

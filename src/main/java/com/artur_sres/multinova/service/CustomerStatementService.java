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

@Service
public class CustomerStatementService {
    public record Entry(long id, String type, String description, BigDecimal delta, BigDecimal balance,
                        LocalDateTime effectiveAt, LocalDateTime recordedAt, boolean recovered, boolean reversed, boolean reversible) {}
    public record Page(List<Entry> items, boolean hasMore, int page) {}
    private final JdbcTemplate jdbc;
    private final AuthenticatedUserProvider auth;
    public CustomerStatementService(JdbcTemplate jdbc, AuthenticatedUserProvider auth) { this.jdbc = jdbc; this.auth = auth; }

    @Transactional(readOnly = true)
    public Page statement(long customerId, int page) {
        if (page < 0 || page > 1000000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página inválida.");
        authorize(customerId, false);
        var entries = jdbc.query("""
                WITH movements AS (
                    SELECT l.*, SUM(delta) OVER (ORDER BY CASE WHEN type = 'INITIAL' THEN 0 ELSE 1 END, recorded_at, id) AS balance,
                           EXISTS(SELECT 1 FROM customer_ledger r WHERE r.reversed_entry_id = l.id) AS reversed
                    FROM customer_ledger l WHERE customer_id = ?
                ) SELECT * FROM movements
                ORDER BY CASE WHEN type = 'INITIAL' THEN 0 ELSE 1 END DESC, recorded_at DESC, id DESC LIMIT 26 OFFSET ?
                """, (rs, row) -> new Entry(rs.getLong("id"), rs.getString("type"), rs.getString("description"),
                rs.getBigDecimal("delta"), rs.getBigDecimal("balance"), rs.getTimestamp("effective_at").toLocalDateTime(),
                rs.getTimestamp("recorded_at").toLocalDateTime(), rs.getBoolean("recovered"), rs.getBoolean("reversed"),
                List.of("INITIAL", "DEBT", "PAYMENT").contains(rs.getString("type")) && !rs.getBoolean("reversed")
                        && rs.getBigDecimal("delta").signum() != 0), customerId, page * 25);
        return new Page(entries.stream().limit(25).toList(), entries.size() > 25, page);
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

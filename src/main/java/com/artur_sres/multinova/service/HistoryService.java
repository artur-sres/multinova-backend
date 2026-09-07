package com.artur_sres.multinova.service;

import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class HistoryService {
    public record Entry(Long id, String type, String title, Long entityId, BigDecimal amount,
                        LocalDateTime occurredAt, boolean recovered) {}
    private final JdbcTemplate jdbc;
    private final AuthenticatedUserProvider auth;
    public HistoryService(JdbcTemplate jdbc, AuthenticatedUserProvider auth) { this.jdbc = jdbc; this.auth = auth; }
    public record Page(List<Entry> items, boolean hasMore, int page) {}

    public Page search(String category, String search, LocalDate from, LocalDate to, int page) {
        if (page < 0 || page > 1000000 || search.length() > 255 || (from != null && to != null && from.isAfter(to)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtros inválidos.");
        String types = switch (category) {
            case "all" -> "";
            case "income" -> "'CASH_ENTRY','CUSTOMER_PAYMENT'";
            case "expense" -> "'BILL_PAID','STOCK_PURCHASE','CASH_EXIT','CUSTOMER_PAYMENT_REVERSED'";
            case "money" -> "'CASH_ENTRY','CUSTOMER_PAYMENT','BILL_PAID','STOCK_PURCHASE','CASH_EXIT','CUSTOMER_PAYMENT_REVERSED'";
            case "stock" -> "'STOCK_PURCHASE','STOCK_ENTRY','SALE_CREATED','SALE_CANCELLED'";
            case "debts" -> "'CUSTOMER_DEBT','CUSTOMER_INITIAL_DEBT','CUSTOMER_DEBT_REVERSED','CUSTOMER_INITIAL_DEBT_REVERSED'";
            case "reversals" -> "'CUSTOMER_PAYMENT_REVERSED','CUSTOMER_DEBT_REVERSED','CUSTOMER_INITIAL_DEBT_REVERSED','SALE_CANCELLED'";
            case "records" -> "'CUSTOMER_CREATED','CUSTOMER_UPDATED','PRODUCT_CREATED','PRODUCT_UPDATED','PRODUCT_DELETED','BILL_CREATED','BILL_DELETED'";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria inválida.");
        };
        var sql = new StringBuilder("SELECT * FROM operation_history WHERE user_id = ?");
        var args = new ArrayList<Object>(); args.add(auth.getCurrentUser().getId());
        if (!types.isEmpty()) sql.append(" AND type IN (").append(types).append(")");
        if (!search.isBlank()) { sql.append(" AND strpos(lower(title), lower(?)) > 0"); args.add(search.trim()); }
        if (from != null) { sql.append(" AND occurred_at >= ?"); args.add(from.atStartOfDay()); }
        if (to != null) { sql.append(" AND occurred_at < ?"); args.add(to.plusDays(1).atStartOfDay()); }
        sql.append(" ORDER BY occurred_at DESC, id DESC LIMIT 26 OFFSET ?"); args.add(page * 25);
        var entries = jdbc.query(sql.toString(), (rs, row) -> new Entry(rs.getLong("id"), rs.getString("type"),
                rs.getString("title"), rs.getLong("entity_id"), rs.getBigDecimal("amount"),
                rs.getTimestamp("occurred_at").toLocalDateTime(), rs.getBoolean("recovered")), args.toArray());
        return new Page(entries.stream().limit(25).toList(), entries.size() > 25, page);
    }
    public List<Entry> recent() {
        return jdbc.query("SELECT * FROM operation_history WHERE user_id = ? ORDER BY occurred_at DESC, id DESC LIMIT 100",
                (rs, row) -> new Entry(rs.getLong("id"), rs.getString("type"), rs.getString("title"), rs.getLong("entity_id"),
                        rs.getBigDecimal("amount"), rs.getTimestamp("occurred_at").toLocalDateTime(), rs.getBoolean("recovered")),
                auth.getCurrentUser().getId());
    }
}

package com.artur_sres.multinova.service;

import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HistoryService {
    public record Entry(Long id, String type, String title, Long entityId, BigDecimal amount,
                        LocalDateTime occurredAt, boolean recovered) {}
    private final JdbcTemplate jdbc;
    private final AuthenticatedUserProvider auth;
    public HistoryService(JdbcTemplate jdbc, AuthenticatedUserProvider auth) { this.jdbc = jdbc; this.auth = auth; }
    public List<Entry> recent() {
        return jdbc.query("SELECT * FROM operation_history WHERE user_id = ? ORDER BY occurred_at DESC, id DESC LIMIT 100",
                (rs, row) -> new Entry(rs.getLong("id"), rs.getString("type"), rs.getString("title"), rs.getLong("entity_id"),
                        rs.getBigDecimal("amount"), rs.getTimestamp("occurred_at").toLocalDateTime(), rs.getBoolean("recovered")),
                auth.getCurrentUser().getId());
    }
}

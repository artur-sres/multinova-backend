package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.dto.cash_entry.CashEntryRequestDTO;
import com.artur_sres.multinova.dto.cash_entry.CashEntryResponseDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class CashEntryRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<CashEntryResponseDTO> MAPPER = (rs, row) -> new CashEntryResponseDTO(
            rs.getLong("id"), rs.getString("title"), rs.getString("kind"), rs.getBigDecimal("amount"),
            rs.getDate("received_on").toLocalDate(), rs.getString("description"), rs.getTimestamp("created_at").toLocalDateTime());

    public CashEntryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public CashEntryResponseDTO create(Long userId, CashEntryRequestDTO dto) {
        return jdbc.queryForObject("INSERT INTO cash_entries (user_id, title, kind, amount, received_on, description) VALUES (?, ?, ?, ?, ?, ?) RETURNING *",
                MAPPER, userId, dto.title().trim(), dto.kind().name(), dto.amount(), dto.receivedOn(), dto.description());
    }

    public List<CashEntryResponseDTO> listRecent(Long userId) {
        return jdbc.query("SELECT * FROM cash_entries WHERE user_id = ? ORDER BY received_on DESC, id DESC LIMIT 100", MAPPER, userId);
    }

    public BigDecimal sumByUserAndPeriod(Long userId, LocalDate from, LocalDate to) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM cash_entries WHERE user_id = ? AND received_on BETWEEN ? AND ?",
                BigDecimal.class, userId, from, to);
    }
}

package com.artur_sres.multinova.repository;

import com.artur_sres.multinova.dto.cash_exit.CashExitRequestDTO;
import com.artur_sres.multinova.dto.cash_exit.CashExitResponseDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class CashExitRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<CashExitResponseDTO> MAPPER = (rs, row) -> new CashExitResponseDTO(
            rs.getLong("id"), rs.getString("title"), rs.getBigDecimal("amount"),
            rs.getDate("paid_on").toLocalDate(), rs.getString("description"), rs.getTimestamp("created_at").toLocalDateTime());

    public CashExitRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public CashExitResponseDTO create(Long userId, CashExitRequestDTO dto) {
        return jdbc.queryForObject("INSERT INTO cash_exits (user_id, title, amount, paid_on, description) VALUES (?, ?, ?, ?, ?) RETURNING *",
                MAPPER, userId, dto.title().trim(), dto.amount(), dto.paidOn(), dto.description());
    }

    public List<CashExitResponseDTO> listRecent(Long userId) {
        return jdbc.query("SELECT * FROM cash_exits WHERE user_id = ? ORDER BY paid_on DESC, id DESC LIMIT 100", MAPPER, userId);
    }

    public BigDecimal sumByUserAndPeriod(Long userId, LocalDate from, LocalDate to) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(amount), 0) FROM cash_exits WHERE user_id = ? AND paid_on BETWEEN ? AND ?",
                BigDecimal.class, userId, from, to);
    }
}

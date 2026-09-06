package com.artur_sres.multinova.service;

import com.artur_sres.multinova.dto.customer.CustomerDebtRequestDTO;
import com.artur_sres.multinova.exception.CustomerNotFoundException;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerDebtService {
    private final JdbcTemplate jdbc;
    private final AuthenticatedUserProvider auth;

    public CustomerDebtService(JdbcTemplate jdbc, AuthenticatedUserProvider auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    @Transactional
    public void create(Long customerId, CustomerDebtRequestDTO dto) {
        // The insert trigger updates the balance and history in this transaction.
        int inserted = jdbc.update("""
                INSERT INTO customer_debts (customer_id, amount, incurred_on, description)
                SELECT id, ?, ?, ? FROM customers WHERE id = ? AND user_id = ?
                """, dto.amount(), dto.incurredOn(), dto.description().trim(), customerId, auth.getCurrentUser().getId());
        if (inserted == 0) throw new CustomerNotFoundException("Customer not found.");
    }
}

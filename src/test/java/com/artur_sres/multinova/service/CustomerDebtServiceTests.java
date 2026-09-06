package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.customer.CustomerDebtRequestDTO;
import com.artur_sres.multinova.dto.customer.CustomerRequestDTO;
import com.artur_sres.multinova.exception.CustomerNotFoundException;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerDebtServiceTests {
    @Test
    void debtCreationIsScopedToCurrentUserAndRejectsMissingOrForeignCustomer() {
        var jdbc = mock(JdbcTemplate.class);
        var auth = mock(AuthenticatedUserProvider.class);
        var user = new User(); user.setId(7L);
        when(auth.getCurrentUser()).thenReturn(user);
        var dto = new CustomerDebtRequestDTO(new BigDecimal("125.50"), LocalDate.now(), " Migration ");
        var service = new CustomerDebtService(jdbc, auth);
        when(jdbc.update(anyString(), eq(dto.amount()), eq(dto.incurredOn()), eq("Migration"), eq(2L), eq(7L))).thenReturn(1);
        assertDoesNotThrow(() -> service.create(2L, dto));
        verify(jdbc).update(contains("WHERE id = ? AND user_id = ?"), eq(dto.amount()), eq(dto.incurredOn()), eq("Migration"), eq(2L), eq(7L));
        assertThrows(CustomerNotFoundException.class, () -> service.create(3L, dto));
    }

    @Test
    void validatesInitialDebtAndManualDebits() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var customer = new CustomerRequestDTO(); customer.setName("Ana");
            for (String amount : new String[]{"-1", "1.001", "100000000"}) {
                customer.setDebtBalance(new BigDecimal(amount));
                assertFalse(validator.validate(customer).isEmpty());
            }
            customer.setDebtBalance(BigDecimal.ZERO);
            assertTrue(validator.validate(customer).isEmpty());
            customer.setDebtBalance(new BigDecimal("125.50"));
            assertTrue(validator.validate(customer).isEmpty());
            assertTrue(validator.validate(new CustomerDebtRequestDTO(BigDecimal.ONE, LocalDate.now(), "Migration")).isEmpty());
            for (String amount : new String[]{"0", "-1", "1.001", "100000000"})
                assertFalse(validator.validate(new CustomerDebtRequestDTO(new BigDecimal(amount), LocalDate.now(), "Migration")).isEmpty());
            assertFalse(validator.validate(new CustomerDebtRequestDTO(BigDecimal.ONE, LocalDate.now().plusDays(1), "Migration")).isEmpty());
            assertFalse(validator.validate(new CustomerDebtRequestDTO(BigDecimal.ONE, LocalDate.now(), " ")).isEmpty());
        }
    }
}

package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.cash_entry.*;
import com.artur_sres.multinova.repository.CashEntryRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CashEntryServiceTests {
    @Test
    void entryAndListingAreScopedToAuthenticatedUser() {
        var repo = mock(CashEntryRepository.class);
        var auth = mock(AuthenticatedUserProvider.class);
        var user = new User(); user.setId(7L);
        when(auth.getCurrentUser()).thenReturn(user);
        var service = new CashEntryService(repo, auth);
        var dto = new CashEntryRequestDTO("Empréstimo", CashEntryRequestDTO.Kind.LOAN, new BigDecimal("500.00"), LocalDate.now(), null);
        service.create(dto);
        service.listRecent();
        verify(repo).create(7L, dto);
        verify(repo).listRecent(7L);
    }
    @Test
    void rejectsInvalidAmountsAndFutureDates() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String value : new String[]{"0", "-1", "1.001", "10000000000"}) {
                assertFalse(validator.validate(new CashEntryRequestDTO("Entrada", CashEntryRequestDTO.Kind.OTHER,
                        new BigDecimal(value), LocalDate.now(), null)).isEmpty());
            }
            assertFalse(validator.validate(new CashEntryRequestDTO("Entrada", CashEntryRequestDTO.Kind.LOAN,
                    BigDecimal.ONE, LocalDate.now().plusDays(1), null)).isEmpty());
            assertTrue(validator.validate(new CashEntryRequestDTO("Entrada", CashEntryRequestDTO.Kind.LOAN,
                    BigDecimal.ONE, LocalDate.now(), null)).isEmpty());
        }
    }
}

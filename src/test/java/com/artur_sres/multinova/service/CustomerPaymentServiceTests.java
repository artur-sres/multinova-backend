package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.*;
import com.artur_sres.multinova.dto.customer_payment.CustomerPaymentRequestDTO;
import com.artur_sres.multinova.exception.CustomerNotFoundException;
import com.artur_sres.multinova.mapper.CustomerPaymentMapper;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerPaymentServiceTests {
    private final CustomerPaymentRepository payments = mock(CustomerPaymentRepository.class);
    private final CustomerRepository customers = mock(CustomerRepository.class);
    private final AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
    private final CustomerPaymentService service =
            new CustomerPaymentService(payments, customers, auth, mock(CustomerPaymentMapper.class));

    private CustomerPaymentRequestDTO request() {
        User user = new User();
        user.setId(1L);
        when(auth.getCurrentUser()).thenReturn(user);
        CustomerPaymentRequestDTO dto = new CustomerPaymentRequestDTO();
        dto.setCustomerId(2L);
        dto.setPaidAt(LocalDateTime.of(2025, 1, 1, 12, 0));
        dto.setAmount(new BigDecimal("50.00"));
        return dto;
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"100.00", "50.00", "25.00", "-25.00"})
    void paymentReducesDebtAndAllowsCredit(String balance) {
        CustomerPaymentRequestDTO dto = request();
        Customer customer = new Customer();
        customer.setDebtBalance(balance == null ? null : new BigDecimal(balance));
        when(customers.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(customer));

        service.create(dto);

        BigDecimal initial = balance == null ? BigDecimal.ZERO : new BigDecimal(balance);
        assertEquals(0, initial.subtract(dto.getAmount()).compareTo(customer.getDebtBalance()));
        ArgumentCaptor<CustomerPayment> captor = ArgumentCaptor.forClass(CustomerPayment.class);
        verify(payments).save(captor.capture());
        assertSame(customer, captor.getValue().getCustomer());
        assertEquals(dto.getPaidAt(), captor.getValue().getPaidAt());
        assertEquals(dto.getAmount(), captor.getValue().getAmount());
    }

    @Test
    void inaccessibleCustomerCannotReceivePaymentsOrExposeHistory() {
        CustomerPaymentRequestDTO dto = request();
        when(customers.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> service.create(dto));
        assertThrows(CustomerNotFoundException.class, () -> service.listByCustomer(2L));
        verifyNoInteractions(payments);
    }

    @Test
    void paymentValidationRejectsInvalidAmountsAndMissingFields() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertFalse(validator.validate(new CustomerPaymentRequestDTO()).isEmpty());
            CustomerPaymentRequestDTO dto = request();
            assertTrue(validator.validate(dto).isEmpty());
            for (String amount : new String[]{"0", "-1", "0.001", "100000000"}) {
                dto.setAmount(new BigDecimal(amount));
                assertFalse(validator.validate(dto).isEmpty());
            }
        }
    }
}

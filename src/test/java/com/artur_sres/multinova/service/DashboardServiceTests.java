package com.artur_sres.multinova.service;

import com.artur_sres.multinova.controller.DashboardController;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTests {
    @Test
    void summaryUsesAuthenticatedUserAndPeriodAndKeepsGlobalBalances() {
        SaleRepository sales = mock(SaleRepository.class);
        CustomerPaymentRepository payments = mock(CustomerPaymentRepository.class);
        StockPurchaseRepository purchases = mock(StockPurchaseRepository.class);
        BillRepository bills = mock(BillRepository.class);
        CustomerRepository customers = mock(CustomerRepository.class);
        AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
        User user = new User();
        user.setId(7L);
        when(auth.getCurrentUser()).thenReturn(user);
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 5);
        var start = from.atStartOfDay();
        var end = to.atTime(23, 59, 59);
        when(sales.sumCompletedByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("900.00"));
        when(payments.sumByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("100.50"));
        when(purchases.sumByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("150.25"));
        when(bills.sumPaidByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("20.10"));
        when(customers.sumDebtBalanceByUser(7L)).thenReturn(new BigDecimal("-50.00"));
        when(bills.sumPendingByUser(7L)).thenReturn(new BigDecimal("300.00"));

        DashboardService service = new DashboardService(sales, payments, purchases, bills, customers, auth);
        var response = new DashboardController(service).getSummary(from, to);
        assertEquals(200, response.getStatusCode().value());
        var summary = response.getBody();
        assertNotNull(summary);
        assertEquals(new BigDecimal("900.00"), summary.getSold());
        assertEquals(new BigDecimal("100.50"), summary.getReceived());
        assertEquals(new BigDecimal("150.25"), summary.getPurchased());
        assertEquals(new BigDecimal("20.10"), summary.getBillsPaid());
        assertEquals(new BigDecimal("-50.00"), summary.getTotalReceivable());
        assertEquals(new BigDecimal("300.00"), summary.getTotalPayable());
        assertEquals(new BigDecimal("-69.85"), summary.getPeriodBalance());
    }
}

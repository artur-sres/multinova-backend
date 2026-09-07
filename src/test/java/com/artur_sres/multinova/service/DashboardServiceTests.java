package com.artur_sres.multinova.service;

import com.artur_sres.multinova.controller.DashboardController;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.repository.CashExitRepository;
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
        CashEntryRepository entries = mock(CashEntryRepository.class);
        AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
        User user = new User();
        user.setId(7L);
        when(auth.getCurrentUser()).thenReturn(user);
        LocalDate from = LocalDate.of(2026, 9, 1);
        LocalDate to = LocalDate.of(2026, 9, 5);
        var start = from.atStartOfDay();
        var end = to.atTime(23, 59, 59);
        when(entries.sumByUserAndPeriod(7L, from, to)).thenReturn(new BigDecimal("1000.00"));
        when(sales.sumCompletedByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("900.00"));
        when(payments.sumByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("100.50"));
        when(purchases.sumByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("150.25"));
        when(purchases.sumPurchasesByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("650.25"));
        when(bills.sumPaidByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("20.10"));
        when(customers.sumDebtBalanceByUser(7L)).thenReturn(new BigDecimal("-50.00"));
        when(bills.sumPendingByUser(7L)).thenReturn(new BigDecimal("300.00"));

        var exits = mock(CashExitRepository.class);
        when(exits.sumByUserAndPeriod(7L, from, to)).thenReturn(new BigDecimal("30.00"));
        DashboardService service = new DashboardService(sales, payments, purchases, bills, customers, auth, entries, exits);
        var response = new DashboardController(service).getSummary(from, to);
        assertEquals(200, response.getStatusCode().value());
        var summary = response.getBody();
        assertNotNull(summary);
        assertEquals(new BigDecimal("900.00"), summary.getSold());
        assertEquals(new BigDecimal("100.50"), summary.getReceived());
        assertEquals(new BigDecimal("650.25"), summary.getPurchased());
        assertEquals(new BigDecimal("170.35"), summary.getBillsPaid());
        assertEquals(new BigDecimal("-50.00"), summary.getTotalReceivable());
        assertEquals(new BigDecimal("300.00"), summary.getTotalPayable());
        assertEquals(new BigDecimal("1000.00"), summary.getOtherIncome());
        assertEquals(new BigDecimal("900.15"), summary.getPeriodBalance());
        assertEquals(new BigDecimal("30.00"), summary.getOtherOutflow());
        verify(exits).sumByUserAndPeriod(7L, from, to);
        verify(entries).sumByUserAndPeriod(7L, from, to);
        // More credit purchases or sales change commercial activity, not cash.
        when(purchases.sumPurchasesByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("5000.00"));
        when(sales.sumCompletedByUserAndPeriod(7L, start, end)).thenReturn(new BigDecimal("9000.00"));
        var updated = service.getSummary(start, end);
        assertEquals(new BigDecimal("5000.00"), updated.getPurchased());
        assertEquals(summary.getPeriodBalance(), updated.getPeriodBalance());
        assertEquals(updated.getReceived().add(updated.getOtherIncome()).subtract(updated.getBillsPaid()).subtract(updated.getOtherOutflow()), updated.getPeriodBalance());
    }
}

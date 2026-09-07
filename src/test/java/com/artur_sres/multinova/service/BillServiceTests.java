package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Bill;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.repository.CashExitRepository;
import com.artur_sres.multinova.domain.enums.BillStatus;
import com.artur_sres.multinova.dto.bill.BillRequestDTO;
import com.artur_sres.multinova.exception.BillNotFoundException;
import com.artur_sres.multinova.mapper.BillMapperImpl;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillServiceTests {
    private final BillRepository bills = mock(BillRepository.class);
    private final AuthenticatedUserProvider auth = mock(AuthenticatedUserProvider.class);
    private final BillService service = new BillService(bills, auth, new BillMapperImpl());

    private BillRequestDTO request(String description) {
        User user = new User();
        user.setId(7L);
        when(auth.getCurrentUser()).thenReturn(user);
        var dto = new BillRequestDTO();
        dto.setName("Aluguel");
        dto.setAmount(new BigDecimal("1200.50"));
        dto.setDueDate(LocalDate.now().plusDays(10));
        dto.setDescription(description);
        return dto;
    }

    @Test
    void descriptionIsOptionalAndReturnedByTheMapper() {
        when(bills.save(any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));
        for (String description : new String[]{null, "Sala comercial"}) {
            var response = service.create(request(description));
            assertEquals(description, response.getDescription());
            assertEquals("Aluguel", response.getName());
            assertEquals(7L, response.getUserId());
            assertEquals(BillStatus.PENDING, response.getStatus());
            assertNull(response.getPaidAt());
        }
    }

    @Test
    void creationPaymentAndDeletionFeedDashboardTotals() {
        var dto = request("Sala comercial");
        List<Bill> stored = new ArrayList<>();
        when(bills.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId(1L);
            stored.add(bill);
            when(bills.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(bill));
            return bill;
        });
        doAnswer(invocation -> { stored.remove(invocation.getArgument(0)); return null; }).when(bills).delete(any(Bill.class));
        when(bills.sumPendingByUser(7L)).thenAnswer(invocation -> stored.stream()
                .filter(b -> b.getStatus() == BillStatus.PENDING).map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        var from = LocalDate.now().atStartOfDay();
        var to = from.plusDays(1).minusNanos(1);
        when(bills.sumPaidByUserAndPeriod(7L, from, to)).thenAnswer(invocation -> stored.stream()
                .filter(b -> b.getStatus() == BillStatus.PAID && !b.getPaidAt().isBefore(from) && !b.getPaidAt().isAfter(to))
                .map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        var sales = mock(SaleRepository.class);
        var payments = mock(CustomerPaymentRepository.class);
        var purchases = mock(StockPurchaseRepository.class);
        var customers = mock(CustomerRepository.class);
        when(sales.sumCompletedByUserAndPeriod(7L, from, to)).thenReturn(BigDecimal.ZERO);
        when(payments.sumByUserAndPeriod(7L, from, to)).thenReturn(BigDecimal.ZERO);
        when(purchases.sumByUserAndPeriod(7L, from, to)).thenReturn(BigDecimal.ZERO);
        when(purchases.sumPurchasesByUserAndPeriod(7L, from, to)).thenReturn(BigDecimal.ZERO);
        var entries = mock(CashEntryRepository.class);
        when(entries.sumByUserAndPeriod(7L, from.toLocalDate(), to.toLocalDate())).thenReturn(BigDecimal.ZERO);
        var exits = mock(CashExitRepository.class);
        when(exits.sumByUserAndPeriod(7L, from.toLocalDate(), to.toLocalDate())).thenReturn(BigDecimal.ZERO);
        var dashboard = new DashboardService(sales, payments, purchases, bills, customers, auth, entries, exits);

        service.create(dto);
        assertEquals(dto.getAmount(), dashboard.getSummary(from, to).getTotalPayable());
        assertEquals(BigDecimal.ZERO, dashboard.getSummary(from, to).getPeriodBalance());
        var paid = service.markAsPaid(1L);
        assertEquals(BillStatus.PAID, paid.getStatus());
        assertNotNull(paid.getPaidAt());
        assertEquals(paid.getPaidAt(), service.markAsPaid(1L).getPaidAt(), "Repeated payment preserves the original date");
        assertEquals(dto.getAmount(), dashboard.getSummary(from, to).getBillsPaid());
        assertEquals(dto.getAmount().negate(), dashboard.getSummary(from, to).getPeriodBalance());
        assertEquals(BigDecimal.ZERO, dashboard.getSummary(from, to).getTotalPayable());
        service.delete(1L);
        assertEquals(BigDecimal.ZERO, dashboard.getSummary(from, to).getBillsPaid());
        assertEquals(BigDecimal.ZERO, dashboard.getSummary(from, to).getPeriodBalance());
    }

    @Test
    void unauthorizedBillCannotBeReadPaidOrDeletedAndListingUsesCurrentUser() {
        request(null);
        when(bills.findByIdAndUserId(1L, 7L)).thenReturn(Optional.empty());
        assertThrows(BillNotFoundException.class, () -> service.searchById(1L));
        assertThrows(BillNotFoundException.class, () -> service.markAsPaid(1L));
        assertThrows(BillNotFoundException.class, () -> service.delete(1L));
        verify(bills, never()).delete(any(Bill.class));
        when(bills.findByUserId(7L)).thenReturn(List.of());
        when(bills.findByUserIdAndStatus(7L, BillStatus.PENDING)).thenReturn(List.of());
        assertTrue(service.listAll(null).isEmpty());
        assertTrue(service.listAll(BillStatus.PENDING).isEmpty());
        verify(bills).findByUserId(7L);
        verify(bills).findByUserIdAndStatus(7L, BillStatus.PENDING);
    }
}

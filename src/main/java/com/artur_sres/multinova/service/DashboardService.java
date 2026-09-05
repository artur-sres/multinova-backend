package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.dashboard.DashboardSummaryResponseDTO;
import com.artur_sres.multinova.repository.*;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final SaleRepository saleRepository;
    private final CustomerPaymentRepository customerPaymentRepository;
    private final StockPurchaseRepository stockPurchaseRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DashboardService(SaleRepository saleRepository,
                            CustomerPaymentRepository customerPaymentRepository,
                            StockPurchaseRepository stockPurchaseRepository,
                            BillRepository billRepository,
                            CustomerRepository customerRepository,
                            AuthenticatedUserProvider authenticatedUserProvider) {
        this.saleRepository = saleRepository;
        this.customerPaymentRepository = customerPaymentRepository;
        this.stockPurchaseRepository = stockPurchaseRepository;
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public DashboardSummaryResponseDTO getSummary(LocalDateTime from, LocalDateTime to) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Long userId = currentUser.getId();

        BigDecimal sold = saleRepository.sumCompletedByUserAndPeriod(userId, from, to);
        BigDecimal received = customerPaymentRepository.sumByUserAndPeriod(userId, from, to);
        BigDecimal purchased = stockPurchaseRepository.sumByUserAndPeriod(userId, from, to);
        BigDecimal billsPaid = billRepository.sumPaidByUserAndPeriod(userId, from, to);
        BigDecimal totalReceivable = customerRepository.sumDebtBalanceByUser(userId);
        BigDecimal totalPayable = billRepository.sumPendingByUser(userId);

        DashboardSummaryResponseDTO response = new DashboardSummaryResponseDTO();
        response.setSold(sold);
        response.setReceived(received);
        response.setPurchased(purchased);
        response.setBillsPaid(billsPaid);
        response.setTotalReceivable(totalReceivable);
        response.setTotalPayable(totalPayable);
        response.setPeriodBalance(received.subtract(purchased).subtract(billsPaid));
        return response;
    }
}

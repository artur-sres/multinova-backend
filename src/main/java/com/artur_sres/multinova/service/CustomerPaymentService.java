package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Customer;
import com.artur_sres.multinova.domain.CustomerPayment;
import com.artur_sres.multinova.dto.customer_payment.CustomerPaymentRequestDTO;
import com.artur_sres.multinova.dto.customer_payment.CustomerPaymentResponseDTO;
import com.artur_sres.multinova.exception.CustomerNotFoundException;
import com.artur_sres.multinova.mapper.CustomerPaymentMapper;
import com.artur_sres.multinova.repository.CustomerPaymentRepository;
import com.artur_sres.multinova.repository.CustomerRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerPaymentService {
    private final CustomerPaymentRepository customerPaymentRepository;
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CustomerPaymentMapper customerPaymentMapper;

    public CustomerPaymentService(CustomerPaymentRepository customerPaymentRepository,
                                  CustomerRepository customerRepository,
                                  AuthenticatedUserProvider authenticatedUserProvider,
                                  CustomerPaymentMapper customerPaymentMapper) {
        this.customerPaymentRepository = customerPaymentRepository;
        this.customerRepository = customerRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.customerPaymentMapper = customerPaymentMapper;
    }

    private Customer getAuthorizedCustomer(Long customerId) {
        return customerRepository.findByIdAndUserId(customerId, authenticatedUserProvider.getCurrentUser().getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
    }

    @Transactional
    public CustomerPaymentResponseDTO create(CustomerPaymentRequestDTO dto) {
        Customer customer = getAuthorizedCustomer(dto.getCustomerId());

        CustomerPayment payment = new CustomerPayment();
        payment.setCustomer(customer);
        payment.setPaidAt(dto.getPaidAt());
        payment.setAmount(dto.getAmount());

        BigDecimal debtBalance = customer.getDebtBalance() != null
                ? customer.getDebtBalance() : BigDecimal.ZERO;
        customer.setDebtBalance(debtBalance.subtract(dto.getAmount()));

        return customerPaymentMapper.toResponseDTO(customerPaymentRepository.save(payment));
    }

    public List<CustomerPaymentResponseDTO> listByCustomer(Long customerId) {
        Customer customer = getAuthorizedCustomer(customerId);
        return customerPaymentRepository.findByCustomerIdOrderByPaidAtDescIdDesc(customer.getId()).stream()
                .map(customerPaymentMapper::toResponseDTO)
                .toList();
    }
}

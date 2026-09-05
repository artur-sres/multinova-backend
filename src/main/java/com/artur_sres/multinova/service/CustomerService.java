package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.domain.Customer;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.customer.CustomerPatchDTO;
import com.artur_sres.multinova.dto.customer.CustomerRequestDTO;
import com.artur_sres.multinova.dto.customer.CustomerResponseDTO;
import com.artur_sres.multinova.exception.*;
import com.artur_sres.multinova.mapper.CustomerMapper;
import com.artur_sres.multinova.repository.CustomerRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, AuthenticatedUserProvider authenticatedUserProvider, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.customerMapper = customerMapper;
    }

    private Customer getAuthorizedCustomer(Long id, Long userId) {
        return customerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
    }

    public CustomerResponseDTO searchById(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Customer customer = getAuthorizedCustomer(id, currentUser.getId());
        return customerMapper.toResponseDTO(customer);
    }

    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        if(customerRepository.existsByNameAndUserId(dto.getName(), currentUser.getId())) {
            throw new CustomerAlreadyExistsException("A customer with this name already exists.");
        }

        Customer customer = new Customer();
        customer.setUser(currentUser);
        customer.setName(dto.getName());
        customer.setAddress(dto.getAddress());
        customer.setPhone(dto.getPhone());
        customer.setBirthDate(dto.getBirthDate());
        customer.setPhotoUrl(dto.getPhotoUrl());
        customer.setPreferences(dto.getPreferences());
        customer.setDebtBalance(dto.getDebtBalance());

        Customer saved = customerRepository.save(customer);

        return customerMapper.toResponseDTO(saved);
    }

    @Transactional
    public CustomerResponseDTO update(Long id, CustomerPatchDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Customer customer = customerRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));

        if (!currentUser.getId().equals(customer.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }

        if(customerRepository.existsByNameAndUserIdAndIdNot(dto.getName(), currentUser.getId(), id)) {
            throw new CustomerAlreadyExistsException("A customer with this name already exists.");
        }

        if (dto.getName() != null) {customer.setName(dto.getName());}
        if (dto.getAddress() != null) {customer.setAddress(dto.getAddress());}
        if (dto.getPhone() != null) {customer.setPhone(dto.getPhone());}
        if (dto.getBirthDate() != null) {customer.setBirthDate(dto.getBirthDate());}
        if (dto.getPhotoUrl() != null) {customer.setPhotoUrl(dto.getPhotoUrl());}
        if (dto.getPreferences() != null) {customer.setPreferences(dto.getPreferences());}

        return customerMapper.toResponseDTO(customer);
    }

    @Transactional
    public CustomerResponseDTO delete(Long id) {
        return null;
    }

    public List<CustomerResponseDTO> listAll() {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        List<Customer> customers = customerRepository.findByUserId(currentUser.getId());

        return customers.stream()
                .map(customerMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}

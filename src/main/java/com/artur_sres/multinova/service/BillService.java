package com.artur_sres.multinova.service;

import com.artur_sres.multinova.domain.Bill;
import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.domain.enums.BillStatus;
import com.artur_sres.multinova.dto.bill.BillRequestDTO;
import com.artur_sres.multinova.dto.bill.BillResponseDTO;
import com.artur_sres.multinova.exception.BillNotFoundException;
import com.artur_sres.multinova.mapper.BillMapper;
import com.artur_sres.multinova.repository.BillRepository;
import com.artur_sres.multinova.security.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillService {
    private final BillRepository billRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final BillMapper billMapper;

    public BillService(BillRepository billRepository, AuthenticatedUserProvider authenticatedUserProvider, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.billMapper = billMapper;
    }

    private Bill getAuthorizedBill(Long id, Long userId) {
        return billRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found."));
    }

    @Transactional
    public BillResponseDTO create(BillRequestDTO dto) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Bill bill = new Bill();
        bill.setUser(currentUser);
        bill.setName(dto.getName());
        bill.setDescription(dto.getDescription());
        bill.setAmount(dto.getAmount());
        bill.setDueDate(dto.getDueDate());

        return billMapper.toResponseDTO(billRepository.save(bill));
    }

    public BillResponseDTO searchById(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return billMapper.toResponseDTO(getAuthorizedBill(id, currentUser.getId()));
    }

    @Transactional
    public BillResponseDTO markAsPaid(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Bill bill = getAuthorizedBill(id, currentUser.getId());
        if (bill.getStatus() != BillStatus.PAID) {
            bill.setStatus(BillStatus.PAID);
            bill.setPaidAt(LocalDateTime.now());
        }
        return billMapper.toResponseDTO(bill);
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Bill bill = getAuthorizedBill(id, currentUser.getId());
        billRepository.delete(bill);
    }

    public List<BillResponseDTO> listAll(BillStatus status) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        List<Bill> bills = status == null
                ? billRepository.findByUserId(currentUser.getId())
                : billRepository.findByUserIdAndStatus(currentUser.getId(), status);

        return bills.stream()
                .map(billMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}

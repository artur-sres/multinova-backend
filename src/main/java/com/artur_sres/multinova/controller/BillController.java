package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.bill.BillRequestDTO;
import com.artur_sres.multinova.dto.bill.BillResponseDTO;
import com.artur_sres.multinova.domain.enums.BillStatus;
import com.artur_sres.multinova.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public ResponseEntity<BillResponseDTO> createBill(@Valid @RequestBody BillRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDTO> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.searchById(id));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<BillResponseDTO> markBillAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(billService.markAsPaid(id));
    }

    @GetMapping
    public ResponseEntity<List<BillResponseDTO>> listBills(@RequestParam(required = false) BillStatus status) {
        return ResponseEntity.ok(billService.listAll(status));
    }
}

package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.bill.BillRequestDTO;
import com.artur_sres.multinova.dto.bill.BillResponseDTO;
import com.artur_sres.multinova.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bills")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping
    public ResponseEntity<BillResponseDTO> createBill(@Valid @RequestBody BillRequestDTO dto) {
        return null;
    }

}

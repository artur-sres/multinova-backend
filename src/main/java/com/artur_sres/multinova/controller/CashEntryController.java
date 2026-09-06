package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.cash_entry.*;
import com.artur_sres.multinova.service.CashEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cash-entries")
public class CashEntryController {
    private final CashEntryService service;
    public CashEntryController(CashEntryService service) { this.service = service; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashEntryResponseDTO create(@Valid @RequestBody CashEntryRequestDTO dto) { return service.create(dto); }
    @GetMapping
    public List<CashEntryResponseDTO> list() { return service.listRecent(); }
}

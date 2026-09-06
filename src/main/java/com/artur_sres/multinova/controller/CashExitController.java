package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.dto.cash_exit.*;
import com.artur_sres.multinova.service.CashExitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cash-exits")
public class CashExitController {
    private final CashExitService service;
    public CashExitController(CashExitService service) { this.service = service; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashExitResponseDTO create(@Valid @RequestBody CashExitRequestDTO dto) { return service.create(dto); }
    @GetMapping
    public List<CashExitResponseDTO> list() { return service.listRecent(); }
}

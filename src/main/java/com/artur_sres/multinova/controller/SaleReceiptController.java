package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.CustomerStatementService;
import org.springframework.web.bind.annotation.*;

@RestController
public class SaleReceiptController {
    private final CustomerStatementService service;
    public SaleReceiptController(CustomerStatementService service) { this.service = service; }
    @GetMapping("/sales/{id}/receipt")
    public CustomerStatementService.Receipt receipt(@PathVariable long id) { return service.saleReceipt(id); }
}

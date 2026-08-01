package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.SaleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sales")
public class SaleController {
    public final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

}

package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.SaleItemService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sale_items")
public class SaleItemController {
    public final SaleItemService saleItemService;

    public SaleItemController(SaleItemService saleItemService) {
        this.saleItemService = saleItemService;
    }

}

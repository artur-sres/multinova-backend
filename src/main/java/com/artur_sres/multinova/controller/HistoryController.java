package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.HistoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {
    private final HistoryService service;
    public HistoryController(HistoryService service) { this.service = service; }
    @GetMapping
    public List<HistoryService.Entry> recent() { return service.recent(); }
}

package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.HistoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/history")
public class HistoryController {
    private final HistoryService service;
    public HistoryController(HistoryService service) { this.service = service; }
    @GetMapping
    public HistoryService.Page recent(@RequestParam(defaultValue = "all") String category,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam(required = false) java.time.LocalDate from,
                                     @RequestParam(required = false) java.time.LocalDate to,
                                     @RequestParam(defaultValue = "0") int page) {
        return service.search(category, search, from, to, page);
    }
}

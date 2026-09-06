package com.artur_sres.multinova.controller;

import com.artur_sres.multinova.service.CustomerStatementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers/{customerId}/statement")
public class CustomerStatementController {
    public record ReversalRequest(@NotBlank @Size(max = 1000) String reason) {}
    private final CustomerStatementService service;
    public CustomerStatementController(CustomerStatementService service) { this.service = service; }
    @GetMapping
    public CustomerStatementService.Page list(@PathVariable long customerId, @RequestParam(defaultValue = "0") int page) {
        return service.statement(customerId, page);
    }
    @PostMapping("/{entryId}/reverse")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reverse(@PathVariable long customerId, @PathVariable long entryId, @Valid @RequestBody ReversalRequest request) {
        service.reverse(customerId, entryId, request.reason());
    }
}

package com.artur_sres.multinova.dto.cash_entry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CashEntryResponseDTO(Long id, String title, String kind, BigDecimal amount,
                                   LocalDate receivedOn, String description, LocalDateTime createdAt) {}

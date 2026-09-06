package com.artur_sres.multinova.dto.cash_exit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CashExitResponseDTO(Long id, String title, BigDecimal amount,
                                   LocalDate paidOn, String description, LocalDateTime createdAt) {}

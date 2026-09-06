package com.artur_sres.multinova.dto.cash_exit;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CashExitRequestDTO(
        @NotBlank @Size(max = 255) String title,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull @PastOrPresent LocalDate paidOn,
        @Size(max = 5000) String description) {
}

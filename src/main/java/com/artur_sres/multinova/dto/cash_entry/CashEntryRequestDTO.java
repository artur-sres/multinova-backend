package com.artur_sres.multinova.dto.cash_entry;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CashEntryRequestDTO(
        @NotBlank @Size(max = 255) String title,
        @NotNull Kind kind,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull @PastOrPresent LocalDate receivedOn,
        @Size(max = 5000) String description) {
    public enum Kind { LOAN, CAPITAL, OTHER }
}

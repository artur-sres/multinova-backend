package com.artur_sres.multinova.dto.customer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerDebtRequestDTO(
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal amount,
        @NotNull @PastOrPresent LocalDate incurredOn,
        @NotBlank @Size(max = 1000) String description) {}

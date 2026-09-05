package com.artur_sres.multinova.dto.customer_payment;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerPaymentRequestDTO {
    @NotNull(message = "Customer is required.")
    private Long customerId;

    @NotNull(message = "Payment date is required.")
    @PastOrPresent(message = "Payment date cannot be in the future.")
    private LocalDateTime paidAt;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount must be greater than zero.")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places.")
    private BigDecimal amount;

    public Long getCustomerId() {return customerId;}
    public void setCustomerId(Long customerId) {this.customerId = customerId;}

    public LocalDateTime getPaidAt() {return paidAt;}
    public void setPaidAt(LocalDateTime paidAt) {this.paidAt = paidAt;}

    public BigDecimal getAmount() {return amount;}
    public void setAmount(BigDecimal amount) {this.amount = amount;}
}

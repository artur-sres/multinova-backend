package com.artur_sres.multinova.dto.customer_payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerPaymentResponseDTO {
    private Long id;
    private Long customerId;
    private LocalDateTime paidAt;
    private BigDecimal amount;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getCustomerId() {return customerId;}
    public void setCustomerId(Long customerId) {this.customerId = customerId;}

    public LocalDateTime getPaidAt() {return paidAt;}
    public void setPaidAt(LocalDateTime paidAt) {this.paidAt = paidAt;}

    public BigDecimal getAmount() {return amount;}
    public void setAmount(BigDecimal amount) {this.amount = amount;}
}

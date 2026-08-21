package com.artur_sres.multinova.dto.bill;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillRequestDTO {
    @NotBlank(message = "Name is required.")
    private String name;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount must be greater than zero.")
    private BigDecimal amount;

    @NotNull(message = "Due date is required.")
    @FutureOrPresent(message = "Due date must be today or in the future.")
    private LocalDate dueDate;

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public BigDecimal getAmount() {return amount;}
    public void setAmount(BigDecimal amount) {this.amount = amount;}

    public LocalDate getDueDate() {return dueDate;}
    public void setDueDate(LocalDate dueDate) {this.dueDate = dueDate;}
}

package com.artur_sres.multinova.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_payments")
public class CustomerPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Customer getCustomer() {return customer;}
    public void setCustomer(Customer customer) {this.customer = customer;}

    public LocalDateTime getPaidAt() {return paidAt;}
    public void setPaidAt(LocalDateTime paidAt) {this.paidAt = paidAt;}

    public BigDecimal getAmount() {return amount;}
    public void setAmount(BigDecimal amount) {this.amount = amount;}
}

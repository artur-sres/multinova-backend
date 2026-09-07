package com.artur_sres.multinova.domain;

import jakarta.persistence.*;
import com.artur_sres.multinova.domain.enums.StockEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_purchases")
public class StockPurchase {
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 20)
    private StockEntryType entryType = StockEntryType.ADJUSTMENT;

    public StockEntryType getEntryType() {return entryType;}
    public void setEntryType(StockEntryType entryType) {this.entryType = entryType;}


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "stockPurchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockPurchaseItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.purchaseDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}


    public BigDecimal getTotalAmount() {return totalAmount;}
    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}
    public void addToTotalAmount(BigDecimal amount) {
        if (amount != null) {
            this.totalAmount = this.totalAmount.add(amount);
        }
    }

    public LocalDateTime getPurchaseDate() {return purchaseDate;}
    public void setPurchaseDate(LocalDateTime purchaseDate) {this.purchaseDate = purchaseDate;}

    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}

    public List<StockPurchaseItem> getItems() {return items;}
    public void setItems(List<StockPurchaseItem> items) {this.items = items;}
    public void addItem(StockPurchaseItem stockPurchaseItem) {this.items.add(stockPurchaseItem);}

}


package com.artur_sres.multinova.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_purchase_items")
public class StockPurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_purchase_id", nullable = false)
    private StockPurchase stockPurchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public StockPurchase getStockPurchase() {return stockPurchase;}
    public void setStockPurchase(StockPurchase stockPurchase) {this.stockPurchase = stockPurchase;}

    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitCost() {return unitCost;}
    public void setUnitCost(BigDecimal unitCost) {this.unitCost = unitCost;}

    public BigDecimal getSubtotal() {return subtotal;}
    public void setSubtotal(BigDecimal subtotal) {this.subtotal = subtotal;}
}

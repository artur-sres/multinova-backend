package com.artur_sres.multinova.domain;

import jakarta.persistence.*;
import com.artur_sres.multinova.domain.enums.DiscountType;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
public class SaleItem {
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType = DiscountType.NONE;
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;
    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public DiscountType getDiscountType() {return discountType;}
    public void setDiscountType(DiscountType value) {discountType = value;}
    public BigDecimal getDiscountValue() {return discountValue;}
    public void setDiscountValue(BigDecimal value) {discountValue = value;}
    public BigDecimal getDiscountAmount() {return discountAmount;}
    public void setDiscountAmount(BigDecimal value) {discountAmount = value;}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "product_name", nullable = false)
    private String productName;
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Sale getSale() {return sale;}
    public void setSale(Sale sale) {this.sale = sale;}

    public Product getProduct() {return product;}
    public void setProduct(Product product) {this.product = product;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitPrice() {return unitPrice;}
    public void setUnitPrice(BigDecimal unitPrice) {this.unitPrice = unitPrice;}

    public BigDecimal getSubtotal() {return subtotal;}
    public void setSubtotal(BigDecimal subtotal) {this.subtotal = subtotal;}
}

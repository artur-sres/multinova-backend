package com.artur_sres.multinova.dto.stock_purchase_item;

import java.math.BigDecimal;

public class StockPurchaseItemResponseDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal subtotal;

    public Long getProductId() {return productId;}
    public void setProductId(Long productId) {this.productId = productId;}

    public String getProductName() {return productName;}
    public void setProductName(String productName) {this.productName = productName;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitCost() {return unitCost;}
    public void setUnitCost(BigDecimal unitCost) {this.unitCost = unitCost;}

    public BigDecimal getSubtotal() {return subtotal;}
    public void setSubtotal(BigDecimal subtotal) {this.subtotal = subtotal;}
}


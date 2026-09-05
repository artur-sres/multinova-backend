package com.artur_sres.multinova.dto.stock_purchase_item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class StockPurchaseItemRequestDTO {

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Positive(message = "Quantity must be greater than zero.")
    private Integer quantity;

    @NotNull(message = "Unit cost is required.")
    @PositiveOrZero(message = "Unit cost must be zero or greater.")
    private BigDecimal unitCost;

    public Long getProductId() {return productId;}
    public void setProductId(Long productId) { this.productId = productId;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitCost() {return unitCost;}
    public void setUnitCost(BigDecimal unitCost) {this.unitCost = unitCost;}
}


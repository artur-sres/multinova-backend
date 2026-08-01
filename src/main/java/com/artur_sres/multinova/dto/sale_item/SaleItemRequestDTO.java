package com.artur_sres.multinova.dto.sale_item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class SaleItemRequestDTO {

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Positive(message = "Quantity must be greater than zero.")
    private Integer quantity;

    @NotNull(message = "Unit price is required.")
    @PositiveOrZero(message = "Unit price must be zero or greater.")
    private BigDecimal unitPrice;

    public Long getProductId() {return productId;}
    public void setProductId(Long productId) { this.productId = productId;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitPrice() {return unitPrice;}
    public void setUnitPrice(BigDecimal unitPrice) {this.unitPrice = unitPrice;}
}

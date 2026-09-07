package com.artur_sres.multinova.dto.sale_item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import com.artur_sres.multinova.domain.enums.DiscountType;
import jakarta.validation.constraints.Digits;

public class SaleItemRequestDTO {
    @NotNull
    private DiscountType discountType = DiscountType.NONE;
    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;
    public DiscountType getDiscountType() {return discountType;}
    public void setDiscountType(DiscountType value) {discountType = value;}
    public BigDecimal getDiscountValue() {return discountValue;}
    public void setDiscountValue(BigDecimal value) {discountValue = value;}

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Positive(message = "Quantity must be greater than zero.")
    private Integer quantity;

    @NotNull(message = "Unit price is required.")
    @PositiveOrZero(message = "Unit price must be zero or greater.")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal unitPrice;

    public Long getProductId() {return productId;}
    public void setProductId(Long productId) { this.productId = productId;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitPrice() {return unitPrice;}
    public void setUnitPrice(BigDecimal unitPrice) {this.unitPrice = unitPrice;}
}

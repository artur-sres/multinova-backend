package com.artur_sres.multinova.dto.price;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class PriceRequestDTO {
    @NotNull(message = "Purchase price is required.")
    @PositiveOrZero(message = "Purchase price must be zero or greater.")
    private BigDecimal purchasePrice;

    @NotNull(message = "Sale price is required.")
    @PositiveOrZero(message = "Sale price must be zero or greater.")
    private BigDecimal salePrice;

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }
}



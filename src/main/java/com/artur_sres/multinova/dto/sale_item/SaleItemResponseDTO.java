package com.artur_sres.multinova.dto.sale_item;

import java.math.BigDecimal;

public class SaleItemResponseDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public Long getProductId() {return productId;}
    public void setProductId(Long productId) {this.productId = productId;}

    public String getProductName() {return productName;}
    public void setProductName(String productName) {this.productName = productName;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public BigDecimal getUnitPrice() {return unitPrice;}
    public void setUnitPrice(BigDecimal unitPrice) {this.unitPrice = unitPrice;}

    public BigDecimal getSubtotal() {return subtotal;}
    public void setSubtotal(BigDecimal subtotal) {this.subtotal = subtotal;}
}

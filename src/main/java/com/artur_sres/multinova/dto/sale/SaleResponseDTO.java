package com.artur_sres.multinova.dto.sale;

import com.artur_sres.multinova.dto.sale_item.SaleItemResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleResponseDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    private List<SaleItemResponseDTO> items;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getCustomerId() {return customerId;}
    public void setCustomerId(Long customerId) {this.customerId = customerId;}

    public String getCustomerName() {return customerName;}
    public void setCustomerName(String customerName) {this.customerName = customerName;}

    public BigDecimal getTotalAmount() {return totalAmount;}
    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}

    public LocalDateTime getSaleDate() {return saleDate;}
    public void setSaleDate(LocalDateTime saleDate) {this.saleDate = saleDate;}

    public List<SaleItemResponseDTO> getItems() {return items;}
    public void setItems(List<SaleItemResponseDTO> items) {this.items = items;}
}

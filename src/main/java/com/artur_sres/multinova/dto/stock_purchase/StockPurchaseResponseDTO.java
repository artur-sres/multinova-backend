package com.artur_sres.multinova.dto.stock_purchase;

import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemResponseDTO;
import com.artur_sres.multinova.domain.enums.StockEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class StockPurchaseResponseDTO {
    private StockEntryType entryType;
    public StockEntryType getEntryType() {return entryType;}
    public void setEntryType(StockEntryType entryType) {this.entryType = entryType;}
    private Long id;
    private BigDecimal totalAmount;
    private LocalDateTime purchaseDate;
    private List<StockPurchaseItemResponseDTO> items;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public BigDecimal getTotalAmount() {return totalAmount;}
    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}

    public LocalDateTime getPurchaseDate() {return purchaseDate;}
    public void setPurchaseDate(LocalDateTime purchaseDate) {this.purchaseDate = purchaseDate;}

    public List<StockPurchaseItemResponseDTO> getItems() {return items;}
    public void setItems(List<StockPurchaseItemResponseDTO> items) {this.items = items;}
}

package com.artur_sres.multinova.dto.stock_purchase;

import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class StockPurchaseRequestDTO {
    @NotEmpty(message = "A stock purchase must have at least one item.")
    @Valid
    private List<StockPurchaseItemRequestDTO> items;

    public List<StockPurchaseItemRequestDTO> getItems() {return items;}
    public void setItems(List<StockPurchaseItemRequestDTO> items) {this.items = items;}
}

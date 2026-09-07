package com.artur_sres.multinova.dto.stock_purchase;

import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.artur_sres.multinova.domain.enums.StockEntryType;
import java.time.LocalDate;

import java.util.List;

public class StockPurchaseRequestDTO {
    @NotNull
    private StockEntryType entryType = StockEntryType.ADJUSTMENT;
    private LocalDate dueDate;

    public StockEntryType getEntryType() {return entryType;}
    public void setEntryType(StockEntryType entryType) {this.entryType = entryType;}
    public LocalDate getDueDate() {return dueDate;}
    public void setDueDate(LocalDate dueDate) {this.dueDate = dueDate;}

    @NotEmpty(message = "A stock purchase must have at least one item.")
    @Valid
    private List<StockPurchaseItemRequestDTO> items;

    public List<StockPurchaseItemRequestDTO> getItems() {return items;}
    public void setItems(List<StockPurchaseItemRequestDTO> items) {this.items = items;}
}

package com.artur_sres.multinova.dto.sale;

import com.artur_sres.multinova.dto.sale_item.SaleItemRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class SaleRequestDTO {

    @NotNull(message = "Customer is required.")
    private Long customerId;

    @NotEmpty(message = "A sale must have at least one item.")
    @Valid
    private List<SaleItemRequestDTO> items;


}

package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.StockPurchaseItem;
import com.artur_sres.multinova.dto.stock_purchase_item.StockPurchaseItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockPurchaseItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    StockPurchaseItemResponseDTO toResponseDTO(StockPurchaseItem stockPurchaseItem);

    List<StockPurchaseItemResponseDTO> toResponseDTOList(List<StockPurchaseItem> stockPurchaseItems);
}


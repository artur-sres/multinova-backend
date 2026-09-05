package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.StockPurchase;
import com.artur_sres.multinova.dto.stock_purchase.StockPurchaseResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = StockPurchaseItemMapper.class)
public interface StockPurchaseMapper {
    @Mapping(source = "items", target = "items")
    StockPurchaseResponseDTO toResponseDTO(StockPurchase stockPurchase);
}

package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.SaleItem;
import com.artur_sres.multinova.dto.sale_item.SaleItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SaleItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "productName", target = "productName")
    SaleItemResponseDTO toResponseDTO(SaleItem saleItem);

    List<SaleItemResponseDTO> toResponseDTOList(List<SaleItem> saleItems);
}

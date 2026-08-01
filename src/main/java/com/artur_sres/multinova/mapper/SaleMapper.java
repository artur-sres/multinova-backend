package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Sale;
import com.artur_sres.multinova.dto.sale.SaleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    @Mapping(source = "items", target = "items")
    SaleResponseDTO toResponseDTO(Sale sale);
}

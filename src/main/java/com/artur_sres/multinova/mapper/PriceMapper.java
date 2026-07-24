package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Price;
import com.artur_sres.multinova.dto.price.PriceResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PriceMapper {

    @Mapping(source = "product.id", target = "productId")
    PriceResponseDTO toResponseDTO(Price price);
}
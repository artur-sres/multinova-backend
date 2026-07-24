package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Brand;
import com.artur_sres.multinova.dto.brand.BrandResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(source = "user.id", target = "userId")
    BrandResponseDTO toResponseDTO(Brand brand);
}
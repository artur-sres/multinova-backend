package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Category;
import com.artur_sres.multinova.dto.category.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "user.id", target = "userId")
    CategoryResponseDTO toResponseDTO(Category category);
}
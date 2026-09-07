package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Product;
import com.artur_sres.multinova.dto.product.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "imageUrl", expression = "java(product.getImageKey() == null ? product.getImageUrl() : \"/products/\" + product.getId() + \"/image?v=\" + product.getImageKey())")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDTO toResponseDTO(Product product);
}
package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Customer;
import com.artur_sres.multinova.dto.customer.CustomerResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "photoUrl", expression = "java(customer.getImageKey() == null ? customer.getPhotoUrl() : \"/customers/\" + customer.getId() + \"/photo?v=\" + customer.getImageKey())")
    @Mapping(source = "user.id", target = "userId")
    CustomerResponseDTO toResponseDTO(Customer customer);
}

package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.CustomerPayment;
import com.artur_sres.multinova.dto.customer_payment.CustomerPaymentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerPaymentMapper {
    @Mapping(source = "customer.id", target = "customerId")
    CustomerPaymentResponseDTO toResponseDTO(CustomerPayment payment);
}

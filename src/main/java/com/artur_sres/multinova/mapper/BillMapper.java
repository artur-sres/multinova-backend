package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.Bill;
import com.artur_sres.multinova.dto.bill.BillResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillMapper {
    @Mapping(source = "user.id", target = "userId")
    BillResponseDTO toResponseDTO(Bill bill);
}

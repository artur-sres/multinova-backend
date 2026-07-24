package com.artur_sres.multinova.mapper;

import com.artur_sres.multinova.domain.User;
import com.artur_sres.multinova.dto.user.UserResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponseDTO(User user);
}
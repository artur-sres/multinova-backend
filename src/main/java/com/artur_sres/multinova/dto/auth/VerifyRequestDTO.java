package com.artur_sres.multinova.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class VerifyRequestDTO {
    @NotBlank
    public String email;

    @NotBlank
    public String code;
}

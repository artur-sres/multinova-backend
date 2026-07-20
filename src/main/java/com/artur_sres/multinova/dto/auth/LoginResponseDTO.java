package com.artur_sres.multinova.dto.auth;

public class LoginResponseDTO {
    private String token;

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
}

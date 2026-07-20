package com.artur_sres.multinova.dto;

import jakarta.validation.constraints.NotBlank;

public class BrandRequestDTO {
    @NotBlank(message = "Name is required.")
    private String name;

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
}

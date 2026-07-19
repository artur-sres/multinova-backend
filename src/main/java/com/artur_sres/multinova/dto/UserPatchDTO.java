package com.artur_sres.multinova.dto;

import jakarta.validation.constraints.Email;

public class UserPatchDTO {

    private String name;

    @Email(message = "Invalid email address.")
    private String email;

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
}

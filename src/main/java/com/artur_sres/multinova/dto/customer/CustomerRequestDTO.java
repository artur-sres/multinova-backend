package com.artur_sres.multinova.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerRequestDTO {
    @NotBlank(message = "Name is required.")
    private String name;

    private String address;

    @Pattern(regexp = "\\d{10,15}", message = "Phone must have 10 to 15 digits.")
    private String phone;

    @Past(message = "Birth date must be in the past.")
    private LocalDate birthDate;

    @URL(message = "Photo URL must be valid.")
    private String photoUrl;

    private String preferences;

    @NotNull(message = "Debt balance is required.")
    private BigDecimal debtBalance;

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}

    public LocalDate getBirthDate() {return birthDate;}
    public void setBirthDate(LocalDate birthDate) {this.birthDate = birthDate;}

    public String getPhotoUrl() {return photoUrl;}
    public void setPhotoUrl(String photoUrl) {this.photoUrl = photoUrl;}

    public String getPreferences() {return preferences;}
    public void setPreferences(String preferences) {this.preferences = preferences;}

    public BigDecimal getDebtBalance() {return debtBalance;}
    public void setDebtBalance(BigDecimal debtBalance) {this.debtBalance = debtBalance;}
}
